package com.semillero.ecosistema.controllers;

import com.semillero.ecosistema.client.CloudinaryRest;
import com.semillero.ecosistema.dtos.CategoryDto;
import com.semillero.ecosistema.dtos.supplier.SupplierDto;
import com.semillero.ecosistema.dtos.supplier.SupplierSearchAndFilterDto;
import com.semillero.ecosistema.request.supplier.SupplierRequest;
import com.semillero.ecosistema.request.supplier.SupplierStatusPatchRequest;
import com.semillero.ecosistema.request.supplier.SupplierUpdateRequest;
import com.semillero.ecosistema.services.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService service;

    @GetMapping("/searchbyname")
    public List<SupplierSearchAndFilterDto> findByName(@RequestParam String name) {
        return service.getSearchByName(name);
    }



    @GetMapping("/searchbycategory")
    public List<SupplierSearchAndFilterDto> findByCategory(@RequestParam("id") Long id) {
        CategoryDto category = new CategoryDto();
        category.setId(id);
        return service.findByCategory(category);
    }

    @GetMapping("/searchallacepted")
    public List<SupplierSearchAndFilterDto> findAllAcepted() {
        return service.findAllStatusAcept();
    }

    @GetMapping("/searchAllReviewAndChange")
    public List<SupplierDto> findAllReviewAndInitial(){
        return service.findAllStatusReviewAndChange();
    }

   @GetMapping("/allDeniedSupplier")
   public List<SupplierDto> findAllSupplierDenied(){
        return service.findAllDeniedSupplier();
   }
    @PostMapping("/create")
    public SupplierDto createSupplier (@Valid @RequestBody SupplierRequest newSupplier){
    return service.createSuppler(newSupplier);
    }

    @PutMapping("/changeStatus/{id}")
    public SupplierDto patchStatus(@Valid @RequestBody SupplierStatusPatchRequest status, @PathVariable Long id){
        return service.patchStatus(id, status);
    }

    @PutMapping("/update")
    public SupplierDto createSupplier ( @Valid @RequestBody SupplierUpdateRequest updateSupplier){
    return service.updateSuppler(updateSupplier);
    }
    
    @GetMapping
    public List<SupplierDto> getAllSupplier (){
    return service.findAllSupplier();
}

    @GetMapping("/allNews")
    public List<SupplierDto> getAllNews(){
            return service.findAllStatusNew();
    }
    @GetMapping("/user/{id}")
        public List<SupplierDto> getAllSupplerByUser(@PathVariable Long id){
            return service.getByUser(id);
        }

    @GetMapping("getById/{id}")
    public SupplierDto getSupplierById(@PathVariable Long id){
        return service.getById(id);
    }

}

