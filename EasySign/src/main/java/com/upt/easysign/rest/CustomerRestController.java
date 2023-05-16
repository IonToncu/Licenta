package com.upt.easysign.rest;


import com.upt.easysign.dto.AuthenticationRequestDto;
import com.upt.easysign.dto.FolderDto;
import com.upt.easysign.dto.UserDto;
import com.upt.easysign.model.file.Document;
import com.upt.easysign.model.file.FileStatus;
import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.file.StackFolder;
import com.upt.easysign.model.user.Customer;
import com.upt.easysign.model.user.Notary;
import com.upt.easysign.repository.file_repository.FolderRepository;
import com.upt.easysign.repository.user_repository.CustomerRepository;
import com.upt.easysign.repository.user_repository.NotaryRepository;
import com.upt.easysign.security.jwt.JwtTokenProvider;
import com.upt.easysign.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api/v1/admin/")
@CrossOrigin("http://localhost:8080")
public class CustomerRestController {

    private final CustomerService customerService;
    private final FolderService folderService;
    private final DocumentService documentService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final FolderRepository folderRepository;
    private final PublicStackFolderService stackFolderService;
    private final NotaryService notaryService;
    private final NotaryRepository notaryRepository;


    public CustomerRestController(CustomerService customerService, FolderService folderService, DocumentService documentService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, CustomerRepository customerRepository, FolderRepository folderRepository, PublicStackFolderService stackFolderService, NotaryService notaryService,
                                  NotaryRepository notaryRepository) {
        this.customerService = customerService;
        this.folderService = folderService;
        this.documentService = documentService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.customerRepository = customerRepository;
        this.folderRepository = folderRepository;
        this.stackFolderService = stackFolderService;
        this.notaryService = notaryService;
        this.notaryRepository = notaryRepository;
    }

    @PostMapping("customer_login")
    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
        String emailRegex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        try {
            Customer customer;
            String username = requestDto.getUsername();
            if(pattern.matcher(username).matches()){
                customer = customerService.getCustomerByEmail(username);
                username = customer.getUsername();
            }else customer = customerService.findByUsername(username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            if (customer == null) {
                throw new UsernameNotFoundException("User with username: " + username + " not found");
            }

            String token = jwtTokenProvider.createToken(username, customer.getRoles());

            Map<Object, Object> response = new HashMap<>();
            response.put("FullName", customer.getFirstName() + " " + customer.getLastName());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @PostMapping("customer_registration")
    public ResponseEntity registrationCustomer(@RequestBody UserDto requestDto) {
        if(userService.containUserByEmail(requestDto.getEmail())) throw new BadCredentialsException("Email already exist in DB");
        if(userService.findByUsername(requestDto.getUsername()) != null){
            requestDto.setUsername(requestDto.getUsername() + UUID.randomUUID());
        }
        if(requestDto.getPassword().isEmpty()) throw new BadCredentialsException("Password is empty");
        Customer customer = requestDto.toCustomer();
        customerService.register(customer);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Ok");
        response.put("username", requestDto.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("customer_creation_folder")
    public ResponseEntity createFolder(@RequestBody FolderDto folderDto) {
        Folder folder = folderDto.toFolder();
        folder = folderService.save(folder);
        Customer customer = customerService.findByUsername(folderDto.getOwnerUsername());
        customerService.addFolder(customer, folder);
        customerRepository.save(customer);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Ok");
        response.put("Owner", folderDto.getOwnerUsername());
        response.put("File name", folderDto.getFileName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("customer_add_Document_to_Folder")
    public ResponseEntity<Folder> addDocumentInFolder(@RequestParam("file") MultipartFile file,
                                              @RequestParam("username") String username,
                                              @RequestParam("filename") String filename,
                                              @RequestParam("folderId") long folderId) throws Exception {

        Folder folder = getFolderFromCustomerById(username, folderId);
        Document document = new Document();
        document.setFile(file.getBytes());
        document.setStatus(FileStatus.PENDING);
        document.setFileName(filename);
        document.setUpdated(new Date());
        document.setCreated(new Date());

        document = documentService.save(document);
        folder.addDocumentInList(document);
        folder = folderRepository.save(folder);
        return new ResponseEntity<>(folder, HttpStatus.OK);
    }

    private Folder getFolderFromCustomerById(@RequestParam("username") String username, @RequestParam("folderId") long folderId) throws Exception {
        Customer customer = customerService.findByUsername(username);
        Folder folder = null;
        for (Folder personalListOfFolder : customer.getPersonalListOfFolders()) {
            if(personalListOfFolder.getId() == folderId){
                folder = personalListOfFolder;
                break;
            }
        }
        if(folder == null) throw new Exception("Folder not found");
        return folder;
    }

    @PostMapping("customer_post_Folder")
    public ResponseEntity<FolderDto> postFolderPublic(@RequestBody FolderDto folderDto) throws Exception {
        Folder folder = getFolderFromCustomerById(folderDto.getOwnerUsername(), folderDto.getId());
        StackFolder stackFolder = stackFolderService.post(StackFolder.createStackFolder(folder));
        return new ResponseEntity<>(stackFolder.toFolderDto(), HttpStatus.OK);
    }
    @GetMapping("/candidate/doc/{docId}")
    @ResponseBody
    public ResponseEntity<byte[]> getProveDocumentOfCandidate(@PathVariable long docId) throws IOException {
        Document document = documentService.getById(docId);
        byte[] bytes = document.getFile();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        String filename = document.getFileName();
        headers.add("content-disposition", "inline;filename=" + filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
        return response;
    }

    @PostMapping("customer/folders")
    public ResponseEntity getFolders(@RequestBody UserDto requestDto) {
        System.out.println(requestDto);
       Customer customer = customerService.findByUsername(requestDto.getUsername());
        Map<Object, Object> response = new HashMap<>();
        int peFolders = 0, deFolders = 0, apFolders = 0;
        for (Folder folder : customer.getPersonalListOfFolders()) {
            if(folder.getStatus() == FileStatus.PENDING) peFolders++;
            if(folder.getStatus() == FileStatus.DENIED) deFolders++;
            if(folder.getStatus() == FileStatus.CHECKED) apFolders++;
        }
        List<Folder> folders = customer.getPersonalListOfFolders();
        List<FolderDto> foldersDto = new ArrayList<>();
        folders.forEach(folder -> {
            foldersDto.add(folder.toFolderDto());
        });

        response.put("folders", foldersDto);
        response.put("PENDING", peFolders);
        response.put("DENIED", deFolders);
        response.put("CHECKED", apFolders);

        return ResponseEntity.ok(response);
    }

    @PostMapping("customer/choose_notary_to_sign")
    public ResponseEntity<Notary> chooseNotaryNoSign(@RequestParam("customerUsername") String customerUsername,
                                                          @RequestParam("notaryUsername") String notaryUsername,
                                                          @RequestParam("folderId") long folderId) throws Exception {

        Folder folder = getFolderFromCustomerById(customerUsername, folderId);
        Notary notary = notaryService.findByUsername(notaryUsername);
        notary.addFolder(folder);
        notary = notaryRepository.save(notary);
        return new ResponseEntity<>(notary, HttpStatus.OK);
    }

}
