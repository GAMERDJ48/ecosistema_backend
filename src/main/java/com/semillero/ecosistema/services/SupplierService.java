package com.semillero.ecosistema.services;

import com.semillero.ecosistema.client.CloudinaryRest;
import com.semillero.ecosistema.dtos.CategoryDto;
import com.semillero.ecosistema.dtos.PaisDto;
import com.semillero.ecosistema.dtos.supplier.SupplierDto;
import com.semillero.ecosistema.dtos.supplier.SupplierSearchAndFilterDto;
import com.semillero.ecosistema.dtos.ProvinciaDto;
import com.semillero.ecosistema.enums.Status;
import com.semillero.ecosistema.exceptions.ResourceNotFoundException;
import com.semillero.ecosistema.models.*;
import com.semillero.ecosistema.repositories.IUserRepository;
import com.semillero.ecosistema.repositories.SupplierRepository;
import com.semillero.ecosistema.request.ImageRequest;
import com.semillero.ecosistema.request.supplier.SupplierRequest;
import com.semillero.ecosistema.request.supplier.SupplierStatusPatchRequest;
import com.semillero.ecosistema.request.supplier.SupplierUpdateRequest;
import jakarta.transaction.Transactional;
import org.hibernate.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class SupplierService {
    @Autowired
    private SupplierRepository repository;
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ImageService iamgeService;

    //Filtro por contiene get y no esten eliminados;
    public List<SupplierSearchAndFilterDto> getSearchByName(String name){
       List<SupplierModel>  suppliers = repository.findAllByNameContainingOrShortDescriptionContainingAndDeletedFalse(name);

        return this.listSupplier(suppliers);

    }

    public List<SupplierDto> findAllSupplier(){
        List<SupplierModel> listSupplier = repository.findAll();

        List<SupplierDto> listSupplierDto = new ArrayList<>();
        for(SupplierModel supplier:listSupplier){
            listSupplierDto.add(getSupplierDto(supplier));
        }

        return listSupplierDto;
    }
    @Transactional
    public SupplierDto createSuppler (SupplierRequest newSupplier){
        SupplierModel validSupplier = newSupplier.ToSupplierModel();
        if(repository.countByUserAndDeletedFalse(validSupplier.getUser())>2){
            throw new ResourceNotFoundException("You cannot have more than 3 providers per user.");
        }
        if(!userRepository.existsById(validSupplier.getUser().getId())){
            throw new ResourceNotFoundException("Username does not exist");

        }
        validSupplier.setStatus(Status.valueOf("CAMBIOS_REALIZADOS"));
        SupplierModel supplier = repository.save(validSupplier);

        List<ImageModel> listImageCloudinary = new ArrayList<>();
       for (ImageRequest image :newSupplier.images()){

           listImageCloudinary.add(iamgeService.createdOrUpdateImage(image, supplier));
       }
        validSupplier.setImages(listImageCloudinary);

        return  getSupplierDto(supplier);

    }

    @Transactional
    public SupplierDto updateSuppler (SupplierUpdateRequest supplier){
        Optional<SupplierModel> optionalSupplier = this.repository.findById(supplier.id());
        if (optionalSupplier.isEmpty()){
            throw new ResourceNotFoundException("This provider with id "+ supplier.id() +"does not exist");
        }
        SupplierModel supplierModelDB = optionalSupplier.get();
        SupplierModel supplierModelUpdate = supplier.ToSupplierModel();
        supplierModelUpdate.setFeedback(supplierModelDB.getFeedback());
        supplierModelUpdate.setStatus(supplierModelDB.getStatus());


        List<ImageRequest> imageRequestList = supplier.getImages();
        iamgeService.compareAndDeleteListImageSupplier(imageRequestList,supplierModelUpdate);

        List<ImageModel> listImageCloudinary = new ArrayList<>();
        for(ImageRequest imageRequest:imageRequestList){
            listImageCloudinary.add(iamgeService.createdOrUpdateImage(imageRequest, supplierModelUpdate));
        }
        return this.getSupplierDto(repository.save(supplierModelUpdate));
    }

      public List<SupplierSearchAndFilterDto> findByCategory(CategoryDto categoryDto){
        CategoryModel categoryModel = modelMapper.map(categoryDto,CategoryModel.class);
        Status status = Status.valueOf("ACEPTADO");
        List<SupplierModel> listSupplierModel = repository.findAllByCategoryEqualsAndDeletedFalseAndStatus(categoryModel, status);

        return this.listSupplier(listSupplierModel);
    }

    public List<SupplierSearchAndFilterDto> findAllStatusAcept(){
        Status status = Status.valueOf("ACEPTADO");
        List<SupplierModel> listSupplierModel = repository.findAllByDeletedFalseAndStatus(status);

        return this.listSupplier(listSupplierModel);
    }

    public SupplierDto patchStatus(Long idSupplier, SupplierStatusPatchRequest status){
       Optional<SupplierModel>  supplier = repository.findById(idSupplier);
        if(!supplier.isPresent()) throw new ResourceNotFoundException("this supplier does not exist");

        SupplierModel oSupplier =supplier.get();
        oSupplier.setStatus(status.toStatus());
        oSupplier.setFeedback(status.feedback());
        repository.save(oSupplier);

        return  getSupplierDto(oSupplier);

    }

    public List<SupplierDto> findAllStatusReviewAndChange(){
        Status statusReview = Status.valueOf("REVISION_INICIAL");
        Status statusChange = Status.valueOf("CAMBIOS_REALIZADOS");

        List<SupplierModel> supplierModel = repository.findAllByDeletedIsFalseAndStatusOrStatus(statusReview, statusChange);

        return  getSupplierDtos(supplierModel);
    }

    public List<SupplierDto> findAllStatusNew(){
        Status statusReview = Status.valueOf("REVISION_INICIAL");

        List<SupplierModel> supplierModel = repository.findAllByDeletedIsFalseAndStatusOrStatus(statusReview, statusReview);

        return  getSupplierDtos(supplierModel);
    }
    public List<SupplierDto> findAllDeniedSupplier(){
        Status statusDenied = Status.valueOf("DENEGADO");

        List<SupplierModel> supplierModel = repository.findAllByDeletedIsFalseAndStatusOrStatus(statusDenied, statusDenied);

        return  getSupplierDtos(supplierModel);
    }



    private List<SupplierDto> getSupplierDtos(List<SupplierModel> supplierModel) {
        List<SupplierDto> supplierDtosList = new ArrayList<>();

        for(SupplierModel supplier: supplierModel){
            SupplierDto supplierDto = modelMapper.map(supplier, SupplierDto.class);

            if(supplier.getCountry() != null)
                supplierDto.setCountry(modelMapper.map(supplier.getCountry(), PaisDto.class));

            if(supplier.getProvince() != null)
                supplierDto.setProvince(modelMapper.map(supplier.getProvince(), ProvinciaDto.class));

            if(supplier.getCategory() != null)
                supplierDto.setCategory(modelMapper.map(supplier.getCategory(), CategoryDto.class));


            supplierDtosList.add(supplierDto);
        }
        return supplierDtosList;
    }

    public List<SupplierDto> getByUser(Long user){
        Optional<UserModel> userModel = userRepository.findById(user);
        if(userModel.isEmpty()){
            throw new ResourceNotFoundException("this supplier does not exist");
        }

        List<SupplierModel> suppliers = repository.findAllByUserAndDeletedFalse(userModel.get());

        return this.getSupplierDtos(suppliers);
    }

    public SupplierDto getById(Long id){
       Optional<SupplierModel>  supplier = repository.findById(id);
       if(supplier.isEmpty()) {
           throw new ResourceNotFoundException("this user does not exist");
       }

        return this.getSupplierDto(supplier.get());
    }

    //Metodos Pivados

    private List<SupplierSearchAndFilterDto> listSupplier(List<SupplierModel> listSupplierModel){

        List<SupplierSearchAndFilterDto> listSupplierDto = new ArrayList<>();

        for(SupplierModel supplier:listSupplierModel){
            SupplierSearchAndFilterDto supplierDto = modelMapper.map(supplier, SupplierSearchAndFilterDto.class);
            if(supplier.getCountry() != null)
                supplierDto.setCountry(modelMapper.map(supplier.getCountry(), PaisDto.class));

            if(supplier.getProvince() != null)
                supplierDto.setProvince(modelMapper.map(supplier.getProvince(), ProvinciaDto.class));

            if(supplier.getCategory() != null)
                supplierDto.setCategory(modelMapper.map(supplier.getCategory(), CategoryDto.class));

            listSupplierDto.add(supplierDto);
        }
        return listSupplierDto;
    }

    private SupplierDto getSupplierDto(SupplierModel oSupplier) {
        SupplierDto supplierDto = modelMapper.map(oSupplier, SupplierDto.class);
        if(supplierDto.getCountry() != null)
            supplierDto.setCountry(modelMapper.map(oSupplier.getCountry(), PaisDto.class));

        if(supplierDto.getProvince() != null)
            supplierDto.setProvince(modelMapper.map(oSupplier.getProvince(), ProvinciaDto.class));

        if(supplierDto.getCategory() != null)
            supplierDto.setCategory(modelMapper.map(oSupplier.getCategory(), CategoryDto.class));
        return supplierDto;
    }


}
