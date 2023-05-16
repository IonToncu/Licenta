package com.upt.easysign.service.impl;

import com.upt.easysign.model.NotaryCandidate;
import com.upt.easysign.model.Role;
import com.upt.easysign.model.user.Notary;
import com.upt.easysign.repository.NotaryQueueRepository;
import com.upt.easysign.repository.RoleRepository;
import com.upt.easysign.repository.user_repository.NotaryRepository;
import com.upt.easysign.service.NotaryQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotaryQueueServiceImpl implements NotaryQueueService {
    private final NotaryQueueRepository notaryQueueRepository;
    private final NotaryRepository notaryRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final UserServiceImpl userService;
    @Autowired
    public NotaryQueueServiceImpl(NotaryQueueRepository notaryQueueRepository, NotaryRepository repository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, UserServiceImpl userService) {
        this.notaryQueueRepository = notaryQueueRepository;
        this.notaryRepository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }


    @Override
    public NotaryCandidate addAsCandidate(NotaryCandidate notaryCandidate) {
        notaryCandidate.setPassword(passwordEncoder.encode(notaryCandidate.getPassword()));
        log.info("Added as candidate - notary: {} successfully registered", notaryCandidate);
        return notaryQueueRepository.save(notaryCandidate);
    }

    @Override
    public List<NotaryCandidate> getAll() {
        return notaryQueueRepository.findAll();
    }

    @Override
    public Boolean deleteCandidate(NotaryCandidate notaryCandidate) {
        if(notaryQueueRepository.findById(notaryCandidate.getId()) != null) {
            notaryQueueRepository.delete(notaryCandidate); return true;
        }else{
            log.info("Doesn't exist this candidate", notaryCandidate);
            return false;
        }
    }

    @Override
    public Boolean addCandidateToNotary(NotaryCandidate notaryCandidate) {
        if(userService.containUserByEmail(notaryCandidate.getEmail())) {
            log.info("Email for candidate: {} already is taken", notaryCandidate);
            return false;
        }
        if(userService.findByUsername(notaryCandidate.getUsername()) !=null){
            notaryCandidate.setUsername(notaryCandidate.getUsername() + UUID.randomUUID());
        }

        Role roleUser = roleRepository.findByName("ROLE_NOTARY");
        List<Role> userRoles = new ArrayList<>();
        userRoles.add(roleUser);

        Notary notary = notaryCandidate.toNotary();
        notary.setRoles(userRoles);
        notaryRepository.save(notary);
        log.info("IN register - notary: {} successfully registered", notaryCandidate);
        notaryQueueRepository.delete(notaryCandidate);
        return true;
    }

    @Override
    public NotaryCandidate getNotaryCandidateByEmail(String email) {
        return notaryQueueRepository.findByEmail(email);
    }

    @Override
    public NotaryCandidate getNotaryCandidateById(long id) {
        return notaryQueueRepository.findById(id);
    }
}
