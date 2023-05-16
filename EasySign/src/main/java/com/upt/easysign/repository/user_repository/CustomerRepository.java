package com.upt.easysign.repository.user_repository;

import com.upt.easysign.model.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);
    Customer findByUsername(String username);
    Customer findById(long id);
    Boolean existsByUsername(String username);
}
