package com.upt.easysign.rest;

import com.upt.easysign.dto.UserDto;
import com.upt.easysign.model.NotaryCandidate;
import com.upt.easysign.model.Status;
import com.upt.easysign.model.user.Customer;
import com.upt.easysign.repository.NotaryQueueRepository;
import com.upt.easysign.security.jwt.JwtTokenProvider;
import com.upt.easysign.service.CustomerService;
import com.upt.easysign.service.NotaryQueueService;
import com.upt.easysign.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api/v1/reg/")
@CrossOrigin
public class RegisterRestController{

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;
    private final CustomerService customerService;
    private final NotaryQueueService notaryQueueService;
    private final NotaryQueueRepository notaryQueueRepository;

    public RegisterRestController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, CustomerService customerService, NotaryQueueService notaryQueueService, NotaryQueueRepository notaryQueueRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.customerService = customerService;
        this.notaryQueueService = notaryQueueService;
        this.notaryQueueRepository = notaryQueueRepository;
    }

    @PostMapping("registration")
    public ResponseEntity registration(@RequestBody UserDto requestDto) {
        if(userService.containUserByEmail(requestDto.getEmail())){
            Map<String, String> response = new HashMap<>();
            response.put("Response", "Email already exist in DB");
            return ResponseEntity.ok(response);
        }
        if(userService.findByUsername(requestDto.getUsername()) != null){
            requestDto.setUsername(requestDto.getUsername() + UUID.randomUUID());
        }
        if(requestDto.getPassword().isEmpty()) throw new BadCredentialsException("Password is empty");
        Customer customer = requestDto.toCustomer();
        customer.setCreated(new Date());
        customer.setUpdated(new Date());
        customerService.register(customer);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Ok");
        response.put("username", requestDto.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/registration/notary")
    public ResponseEntity requestNotaryRegistration(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("username") String username,
                                                    @RequestParam("firstName") String firstName,
                                                    @RequestParam("lastName") String lastName,
                                                    @RequestParam("email") String email,
                                                    @RequestParam("password") String password
                                                    ) throws IOException {

        System.out.println("username: " + username);
        System.out.println("email: " + email);
        System.out.println("password: " + password);

        String emailRegex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Map<String, String> response = new HashMap<>();

        if(!pattern.matcher(email).matches()) {
            response.put("Response", "Invalid email");
            return ResponseEntity.ok(response);
        }

        if(password.length() < 5) {
            response.put("Response", "Invalid password");
            return ResponseEntity.ok(response);
        }

        NotaryCandidate notaryCandidate = new NotaryCandidate();
        notaryCandidate.setEmail(email);
        notaryCandidate.setPassword(password);
        notaryCandidate.setFirstName(firstName);
        notaryCandidate.setLastName(lastName);
        notaryCandidate.setCreated(new Date());
        notaryCandidate.setUpdated(new Date());
        notaryCandidate.setStatus(Status.ACTIVE);
        notaryCandidate.setProveDocument(file.getBytes());
        if(notaryQueueRepository.existsByUsername(username))
            notaryCandidate.setUsername(username + UUID.randomUUID());
        else
            notaryCandidate.setUsername(username);

        System.out.println(notaryQueueService.addAsCandidate(notaryCandidate));
        response.put("Response", "Ok");
        response.put("Username", username);
        response.put("Filename", file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

}
