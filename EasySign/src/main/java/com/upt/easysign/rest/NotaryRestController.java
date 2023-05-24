package com.upt.easysign.rest;

import com.upt.easysign.dto.AuthenticationRequestDto;
import com.upt.easysign.dto.FolderDto;
import com.upt.easysign.dto.UserDto;
import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.file.StackFolder;
import com.upt.easysign.model.user.Notary;
import com.upt.easysign.repository.file_repository.FolderRepository;
import com.upt.easysign.repository.user_repository.CustomerRepository;
import com.upt.easysign.repository.user_repository.NotaryRepository;
import com.upt.easysign.security.jwt.JwtTokenProvider;
import com.upt.easysign.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api/v1/admins/")
@CrossOrigin("http://localhost:8080")
public class NotaryRestController {
    private final NotaryService notaryService;
    private final FolderService folderService;
    private final DocumentService documentService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final FolderRepository folderRepository;
    private final PublicStackFolderService stackFolderService;
    private final NotaryRepository notaryRepository;

    public NotaryRestController(NotaryService customerService, FolderService folderService, DocumentService documentService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, CustomerRepository customerRepository, FolderRepository folderRepository, PublicStackFolderService stackFolderService,
                                NotaryRepository notaryRepository) {
        this.notaryService = customerService;
        this.folderService = folderService;
        this.documentService = documentService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.customerRepository = customerRepository;
        this.folderRepository = folderRepository;
        this.stackFolderService = stackFolderService;
        this.notaryRepository = notaryRepository;
    }

    @PostMapping("notary/registration")
    public ResponseEntity registrationCustomer(@RequestBody UserDto requestDto) {
        if(userService.containUserByEmail(requestDto.getEmail())) throw new BadCredentialsException("Email already exist in DB");
        if(userService.findByUsername(requestDto.getUsername()) != null){
            requestDto.setUsername(requestDto.getUsername() + UUID.randomUUID());
        }
        if(requestDto.getPassword().isEmpty()) throw new BadCredentialsException("Password is empty");
        Notary notary = requestDto.toNotary();
        notaryService.register(notary);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Ok");
        response.put("username", requestDto.getUsername());
        return ResponseEntity.ok(response);
    }
    @PostMapping("notary/login")
    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
        String emailRegex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        try {
            Notary notary;
            String username = requestDto.getUsername();
            if(pattern.matcher(username).matches()){
                notary = notaryService.getNotaryByEmail(username);
                username = notary.getUsername();
            }else notary = notaryService.findByUsername(username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            if (notary == null) {
                throw new UsernameNotFoundException("User with username: " + username + " not found");
            }

            String token = jwtTokenProvider.createToken(username, notary.getRoles());

            Map<Object, Object> response = new HashMap<>();
            response.put("FullName", notary.getFirstName() + " " + notary.getLastName());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @GetMapping("notary/allFolders")
    public ResponseEntity getAllFolders(){
        System.out.println(getCurrentUsername());
        Notary notary = notaryService.findByUsername(getCurrentUsername());
        List<Folder> personalFolders = notary.getPersonalListOfFolders();
        List<FolderDto> personalFoldersDto = new ArrayList<>();
        List<StackFolder> publicFolders = stackFolderService.getAll();
        List<FolderDto> publicFoldersDto = new ArrayList<>();

        for (Folder personalFolder : personalFolders) {
            personalFoldersDto.add(personalFolder.toFolderDto());
        }

        for (StackFolder publicFolder : publicFolders) {
            publicFoldersDto.add(publicFolder.toFolderDto());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("publicFolder", publicFoldersDto);
        response.put("personalFolder", personalFoldersDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("notary/addToPersonal")
    public ResponseEntity getAllFolders(@RequestBody FolderDto requestDto) throws Exception {
        Notary notary = notaryService.findByUsername(requestDto.getOwnerUsername());
        StackFolder stackFolder = stackFolderService.getById(requestDto.getId());
        notary.addFolder(stackFolder.getFolder());
        notaryRepository.save(notary);
        stackFolderService.delete(stackFolder);

        Map<String, Object> response = new HashMap<>();
        response.put("notary", notary);
        return ResponseEntity.ok(response);
    }

    @PostMapping("notary/approveFolder")
    public ResponseEntity approveFolder(@RequestBody FolderDto requestDto) throws Exception {
        Folder folder = folderRepository.findById(requestDto.getId());
        folder.setStatus(requestDto.getFileStatus());
        folderRepository.save(folder);
        Map<String, Object> response = new HashMap<>();
        response.put("notary", folder);
        return ResponseEntity.ok(response);
    }
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

}
