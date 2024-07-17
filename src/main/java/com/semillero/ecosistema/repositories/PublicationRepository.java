package com.semillero.ecosistema.repositories;

import com.semillero.ecosistema.models.PublicationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationRepository extends JpaRepository<PublicationModel, Long> {

    boolean existsByTitle(String title);
    List<PublicationModel> findByDeleted(boolean deleted);
    List<PublicationModel> findAllByOrderByQuantityViewsDesc();
}
