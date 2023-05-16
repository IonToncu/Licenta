package com.upt.easysign.service.impl;

import com.upt.easysign.model.Role;
import com.upt.easysign.model.Status;
import com.upt.easysign.model.user.Notary;
import com.upt.easysign.repository.RoleRepository;
import com.upt.easysign.repository.user_repository.NotaryRepository;
import com.upt.easysign.service.NotaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class NotaryServiceImpl implements NotaryService {


    private final NotaryRepository notaryRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public NotaryServiceImpl(NotaryRepository notaryRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.notaryRepository = notaryRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Notary register(Notary notary) {
        Role roleUser = roleRepository.findByName("ROLE_NOTARY");
        List<Role> userRoles = new ArrayList<>();
        userRoles.add(roleUser);

        notary.setPassword(passwordEncoder.encode(notary.getPassword()));
        notary.setRoles(userRoles);
        notary.setStatus(Status.ACTIVE);
        Notary registeredCustomer = notaryRepository.save(notary);

        log.info("IN register - notary: {} successfully registered", registeredCustomer);

        return registeredCustomer;
    }

    @Override
    public List<Notary> getAll() {
        return null;
    }

    @Override
    public Notary findByUsername(String username) {
        return notaryRepository.findByUsername(username);
    }

    @Override
    public Boolean containUserByEmail(String email) {
        return null;
    }

    @Override
    public Notary getNotaryByEmail(String email) {
        return notaryRepository.findByEmail(email);
    }
}
