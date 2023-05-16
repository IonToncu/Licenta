package com.upt.easysign.repository;

import com.upt.easysign.model.NotaryCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaryQueueRepository extends JpaRepository<NotaryCandidate, Long> {
    NotaryCandidate findByEmail(String email);
    NotaryCandidate findById(long id);

    Boolean existsByUsername(String username);
}
