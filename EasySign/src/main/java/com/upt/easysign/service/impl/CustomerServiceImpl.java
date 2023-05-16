package com.upt.easysign.service.impl;

import com.upt.easysign.model.Role;
import com.upt.easysign.model.Status;
import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.user.Customer;
import com.upt.easysign.repository.RoleRepository;
import com.upt.easysign.repository.user_repository.CustomerRepository;
import com.upt.easysign.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Customer register(Customer customer) {
        Role roleUser = roleRepository.findByName("ROLE_CUSTOMER");
        List<Role> userRoles = new ArrayList<>();
        userRoles.add(roleUser);

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRoles(userRoles);
        customer.setStatus(Status.ACTIVE);
        Customer registeredCustomer = customerRepository.save(customer);

        log.info("IN register - customer: {} successfully registered", registeredCustomer);

        return registeredCustomer;
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    @Override
    public Boolean containUserByEmail(String email) {
        return customerRepository.findByEmail(email) != null;
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public void addFolder(Customer customer, Folder folder) {
        customer.addFolder(folder);
    }
}

