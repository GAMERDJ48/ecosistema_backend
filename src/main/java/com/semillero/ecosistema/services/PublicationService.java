package com.semillero.ecosistema.services;

import com.semillero.ecosistema.dtos.PublicationDto;
import com.semillero.ecosistema.dtos.image.ImagePublicDto;
import com.semillero.ecosistema.exceptions.InvalidDataException;
import com.semillero.ecosistema.exceptions.ResourceNotFoundException;
import com.semillero.ecosistema.models.ImageModel;
import com.semillero.ecosistema.models.PublicationModel;
import com.semillero.ecosistema.models.UserModel;
import com.semillero.ecosistema.repositories.PublicationRepository;
import com.semillero.ecosistema.request.ImageRequest;
import com.semillero.ecosistema.request.PublicationRequest;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.semillero.ecosistema.enums.Role.ADMINISTRADOR;

@Service

public class PublicationService {
    private final PublicationRepository publicationRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private ImageService image;

    public PublicationService(PublicationRepository publicationRepository, ModelMapper modelMapper) {
        this.publicationRepository = publicationRepository;

        this.modelMapper = modelMapper;
    }

    //getAll
    public List<PublicationDto> getAll(){

        List<PublicationModel> publicationModels = publicationRepository.findByDeleted(false);
        List<PublicationDto> publicationDtoList = new ArrayList<>();
        for(PublicationModel publication:publicationModels){
            PublicationDto publicationDto = modelMapper.map(publication, PublicationDto.class);
            List<ImagePublicDto> listImages = new ArrayList<>();
            for(ImageModel image:publication.getImages()){
                listImages.add(modelMapper.map(image,ImagePublicDto.class));
            }
            publicationDto.setImagePublicDtoList(listImages);
            publicationDtoList.add(publicationDto);
        }

        return publicationDtoList;
    }


    //gtById
    public PublicationDto getById(Long id_publication, UserModel user) {
        var publicacion = this.publicationRepository.findById(id_publication);
        if (publicacion.isEmpty() || publicacion.get().isDeleted()) {
            throw new ResourceNotFoundException("Publication with id " + id_publication + " does not exist");
        }
        var publicacionModel = publicacion.get();
        increaseViews(1, user, publicacionModel);
        PublicationDto publicationDto = modelMapper.map(publicacionModel, PublicationDto.class);
        List<ImagePublicDto> imagePublicDtoList = new ArrayList<>();

        for(ImageModel imageModel:publicacionModel.getImages()){
            imagePublicDtoList.add(modelMapper.map(imageModel, ImagePublicDto.class));
        }
        publicationDto.setImagePublicDtoList(imagePublicDtoList);

        return publicationDto;
    }

    //create
    @Transactional
    public PublicationDto createPublication(PublicationRequest publication, UserModel user) {

        if (publicationRepository.existsByTitle(publication.getTitle())) {
            throw new ResourceNotFoundException("This title is already created");
        }
        // Verificar si la lista de imágenes está vacía
        if (publication.getImages() == null || publication.getImages().isEmpty()) {
            throw new InvalidDataException("La publicación debe tener al menos una imagen.");
        }

        PublicationModel publicacionModel = modelMapper.map(publication, PublicationModel.class);
        publicacionModel.setUser(user);
        publicacionModel.setDescription(publication.getDescription());

        PublicationModel savedPublication = publicationRepository.save(publicacionModel);
        List<ImageModel> listImageCloudinary = new ArrayList<>();
        // Crear las imágenes asociadas a la publicación
        for (ImageRequest imageRequest : publication.getImages()) {
            listImageCloudinary.add(image.createdOrUpdateImagePublication(imageRequest, savedPublication));
        }
        savedPublication.setImages(listImageCloudinary);
        PublicationDto publicationDto = modelMapper.map(savedPublication, PublicationDto.class);

        publicationDto.setImagePublicDtoList(listImageCloudinary.stream().map(imageModel -> modelMapper.map(imageModel, ImagePublicDto.class)).collect(Collectors.toList()));

        return publicationDto;

    }

    //edit
    @Transactional
    public PublicationModel updatePublication(Long id_publication, PublicationRequest publicacionRequest){
        Optional<PublicationModel> optionalPublication = this.publicationRepository.findById(id_publication);
        if (optionalPublication.isEmpty()){
            throw new ResourceNotFoundException("This publication with id "+ id_publication +"does not exist");
        }
        PublicationModel publication = optionalPublication.get();
        publication.setTitle(publicacionRequest.title());
        publication.setDescription(publicacionRequest.description());

        List<ImageRequest> imageRequestList = publicacionRequest.getImages();
        image.compareAndDeleteListImagePublication(imageRequestList,publication);

        List<ImageModel> listImageCloudinary = new ArrayList<>();
        for(ImageRequest imageRequest:imageRequestList){
                listImageCloudinary.add(image.createdOrUpdateImagePublication(imageRequest, publication));
        }
        return publicationRepository.save(publication);
    }

    // delete
    public PublicationModel deletePublication(Long id){
        var publication = this.publicationRepository.findById(id);
        if (publication.isEmpty()){
            throw new ResourceNotFoundException("This publication with id "+ id +"does not exist");
        }
        publication.get().setDeleted(true);
        return publicationRepository.save(publication.get());
    }

    //incrementar visualizaciones
    public void increaseViews(int numberViews, UserModel user, PublicationModel publicationModel) {
        if (user.getRole().equals(ADMINISTRADOR)){
            publicationRepository.save(publicationModel);
        }else {
            int visual = publicationModel.getQuantityViews() + numberViews;
            publicationModel.setQuantityViews(visual);
            publicationRepository.save(publicationModel);
        }

    }









}
