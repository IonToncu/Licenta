package com.upt.easysign.service;

import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.user.Customer;

import java.util.List;

public interface CustomerService {
    Customer register(Customer customer);
    List<Customer> getAll();
    Customer findByUsername(String username);
    Boolean containUserByEmail(String email);
    Customer getCustomerByEmail(String email);

    void addFolder(Customer customer, Folder folder);
}

