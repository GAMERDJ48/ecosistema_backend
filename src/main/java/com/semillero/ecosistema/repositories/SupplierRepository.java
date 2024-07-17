package com.semillero.ecosistema.repositories;

import com.semillero.ecosistema.enums.Status;
import com.semillero.ecosistema.models.CategoryModel;
import com.semillero.ecosistema.models.SupplierModel;
import com.semillero.ecosistema.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierRepository extends JpaRepository <SupplierModel, Long>
 {
     @Query("SELECT s FROM SupplierModel s WHERE (s.name LIKE %:keyword% OR s.shortDescription LIKE %:keyword%) AND s.deleted = false")
     List<SupplierModel> findAllByNameContainingOrShortDescriptionContainingAndDeletedFalse(String keyword);
     boolean existsById(Long id);
     boolean existsByEmail(String email);
     boolean existsByName(String name);

     List<SupplierModel> findAllByUserAndDeletedFalse(UserModel user);


     List<SupplierModel> findAllByCategoryEqualsAndDeletedFalseAndStatus(CategoryModel Category, Status acepted);

     List<SupplierModel> findAllByDeletedIsFalseAndStatusOrStatus(Status stats1, Status status2);
     List<SupplierModel> findAllByDeletedFalseAndStatus(Status status);
     Integer countByUserAndDeletedFalse(UserModel user);

     List<SupplierModel> findSupplierModelByStatus(Status status);


 }
