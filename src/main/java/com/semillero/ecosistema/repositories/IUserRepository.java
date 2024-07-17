package com.semillero.ecosistema.repositories;

import com.semillero.ecosistema.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<UserModel,Long> {
    boolean existsByEmail(String email);
    UserModel getUserModelByName(String name);

    UserModel findByEmail(String email);
}
