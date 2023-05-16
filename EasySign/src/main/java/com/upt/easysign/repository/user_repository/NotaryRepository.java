package com.upt.easysign.repository.user_repository;


import com.upt.easysign.model.user.Notary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaryRepository extends JpaRepository<Notary, Long> {
    Notary findByEmail(String email);
    Notary findByUsername(String username);
    Notary findById(long id);
    Boolean existsByUsername(String username);
}
