package com.semillero.ecosistema.controllers;

import com.semillero.ecosistema.dtos.PublicationDto;
import com.semillero.ecosistema.exceptions.ResourceNotFoundException;
import com.semillero.ecosistema.models.PublicationModel;
import com.semillero.ecosistema.models.UserModel;
import com.semillero.ecosistema.request.PublicationRequest;
import com.semillero.ecosistema.services.PublicationService;
import com.semillero.ecosistema.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semillero.ecosistema.enums.Role.ADMINISTRADOR;

@RestController
@RequestMapping("/publication")
public class PublicationController {
    private final PublicationService publicationService;
    private UserService userService;

    public PublicationController(PublicationService publicacionService, UserService userService) {
        this.publicationService = publicacionService;
        this.userService = userService;
    }
    @GetMapping
    public List<PublicationDto> getAll(){
        return publicationService.getAll();
    }

    @GetMapping("/getById/{id_publication}/{id_user}")
    public ResponseEntity<PublicationDto> getById(@PathVariable Long id_publication, @PathVariable Long id_user){
        try {
            UserModel user = userService.getById(id_user);
            PublicationDto publication = publicationService.getById(id_publication, user);
            return ResponseEntity.ok(publication);
        }catch (Exception e){
            System.err.println("An error occurred while fetching publication" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/create/{id}")
    public ResponseEntity<PublicationDto> createPublication(@Valid @RequestBody PublicationRequest publicationRequest, @PathVariable Long id){
        try {
            UserModel user = userService.getById(id);
            PublicationDto publication = publicationService.createPublication(publicationRequest, user);
            return ResponseEntity.ok(publication);
        }catch (Exception e){
            System.err.println("An error occurred while creating the publication" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/edit/{id_publication}/{id_user}")
    public PublicationModel editPublicacion(@PathVariable Long id_publication, @PathVariable Long id_user, @RequestBody PublicationRequest publicacionRequest){
        return publicationService.updatePublication(id_publication, publicacionRequest);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PublicationDto> deletePublicacion(@PathVariable Long id){
        try {
            publicationService.deletePublication(id);
            return ResponseEntity.status(200).build();

        }catch (Exception e){
            System.err.println("Deleting the publication failed" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
