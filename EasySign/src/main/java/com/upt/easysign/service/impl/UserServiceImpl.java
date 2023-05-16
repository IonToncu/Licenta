package com.upt.easysign.service.impl;

import com.upt.easysign.model.user.User;
import com.upt.easysign.repository.RoleRepository;
import com.upt.easysign.repository.user_repository.CustomerRepository;
import com.upt.easysign.repository.user_repository.NotaryRepository;
import com.upt.easysign.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final NotaryRepository notaryRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(NotaryRepository notaryRepository, CustomerRepository customerRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.notaryRepository = notaryRepository;
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


//    public User register(User user) {
//        Role roleUser;
//        if(user instanceof Notary){
//            roleUser = roleRepository.findByName("ROLE_NOTARY");
//        }else{
//            roleUser = roleRepository.findByName("ROLE_CUSTOMER");
//        }
//        List<Role> userRoles = new ArrayList<>();
//        userRoles.add(roleUser);
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRoles(userRoles);
//        user.setStatus(Status.ACTIVE);
//        User registeredUser;
//
//        if(user instanceof Notary)registeredUser = notaryRepository.save((Notary) user);
//        else registeredUser = customerRepository.save((Customer) user);
//
//
//        log.info("IN register - user: {} successfully registered", registeredUser);
//
//        return registeredUser;
//    }

    @Override
    public List<User> getAll() {
        List<User> result = new ArrayList<>(notaryRepository.findAll());
        result.addAll(customerRepository.findAll());
        log.info("IN getAll - {} users found", result.size());
        return result;
    }

    @Override
    public User findByUsername(String username) {
        User result = notaryRepository.findByUsername(username);
        if(result == null)  result = customerRepository.findByUsername(username);
        log.info("IN findByUsername - user: {} found by username: {}", result, username);
        return result;
    }




    @Override
    public Boolean containUserByEmail(String email) {
        if((notaryRepository.findByEmail(email) != null) ||
           (customerRepository.findByEmail(email) != null)) {
            log.info("Exist user with this email");
            return true;
        }
        else {
            log.info("Doesn't exist user with this email");
            return false;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        User user = notaryRepository.findByEmail(email);
        if(user != null) return user;
        return customerRepository.findByEmail(email);
    }
}
