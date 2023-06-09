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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api/v1/customer")
@CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowedHeaders = "*", exposedHeaders = "Authorization")
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

//    @PostMapping("customer_login")
//    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
//        String emailRegex = "^(.+)@(.+)$";
//        Pattern pattern = Pattern.compile(emailRegex);
//        try {
//            Customer customer;
//            String username = requestDto.getUsername();
//            if(pattern.matcher(username).matches()){
//                customer = customerService.getCustomerByEmail(username);
//                username = customer.getUsername();
//            }else customer = customerService.findByUsername(username);
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
//            if (customer == null) {
//                throw new UsernameNotFoundException("User with username: " + username + " not found");
//            }
//
//            String token = jwtTokenProvider.createToken(username, customer.getRoles());
//
//            Map<Object, Object> response = new HashMap<>();
//            response.put("FullName", customer.getFirstName() + " " + customer.getLastName());
//            response.put("token", token);
//
//            return ResponseEntity.ok(response);
//        } catch (AuthenticationException e) {
//            throw new BadCredentialsException("Invalid username or password");
//        }
//    }

//    @PostMapping("customer_registration")
//    public ResponseEntity registrationCustomer(@RequestBody UserDto requestDto) {
//        if(userService.containUserByEmail(requestDto.getEmail())) throw new BadCredentialsException("Email already exist in DB");
//        if(userService.findByUsername(requestDto.getUsername()) != null){
//            requestDto.setUsername(requestDto.getUsername() + UUID.randomUUID());
//        }
//        if(requestDto.getPassword().isEmpty()) throw new BadCredentialsException("Password is empty");
//        Customer customer = requestDto.toCustomer();
//        customerService.register(customer);
//        Map<String, String> response = new HashMap<>();
//        response.put("Response", "Ok");
//        response.put("username", requestDto.getUsername());
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("creation_folder")
    public ResponseEntity createFolder(@RequestParam("fileName") String fileName) {
        FolderDto folderDto = new FolderDto();
        folderDto.setFileName(fileName);
        Folder folder = folderDto.toFolder();
        folder.setIsPosted(false);
        folder = folderService.save(folder);
        Customer customer = customerService.findByUsername(getCurrentUsername());
        customerService.addFolder(customer, folder);
        customerRepository.save(customer);
        Map<String, String> response = new HashMap<>();
        response.put("Response", "Ok");
        response.put("Owner", getCurrentUsername());
        response.put("File name", fileName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("add_document")
    public ResponseEntity<FolderDto> addDocumentInFolder(@RequestParam("file") MultipartFile file,
                                              @RequestParam("filename") String filename,
                                              @RequestParam("folderId") long folderId) throws Exception {

         Folder folder = getFolderFromCustomerById(getCurrentUsername(), folderId);
        Document document = new Document();
        document.setFile(file.getBytes());
        document.setStatus(FileStatus.PENDING);
        document.setFileName(filename);
        document.setUpdated(new Date());
        document.setCreated(new Date());

        document = documentService.save(document);
        folder.addDocumentInList(document);
        folder = folderRepository.save(folder);
        return new ResponseEntity<>(folder.toFolderDto(), HttpStatus.OK);
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

    @GetMapping("customer_post_Folder/{folderId}")
    public ResponseEntity<FolderDto> postFolderPublic(@PathVariable long folderId) throws Exception {
        Folder folder = getFolderFromCustomerById(getCurrentUsername(), folderId);
        folder.setIsPosted(true);
        StackFolder stackFolder = stackFolderService.post(StackFolder.createStackFolder(folder));
        return new ResponseEntity<>(stackFolder.toFolderDto(), HttpStatus.OK);
    }

    @GetMapping("folders")
    public ResponseEntity getFolders() {
        Customer customer = customerService.findByUsername(getCurrentUsername());
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

    @GetMapping("folder/{folderId}")
    public ResponseEntity<?> getFolder(@PathVariable long folderId) {
        try {
            Customer customer = customerService.findByUsername(getCurrentUsername());
            Folder folder = folderRepository.getById(folderId);
            if (folder == null) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found if folder not found
            }

            Map<String, Object> response = new HashMap<>();

            if (customer.getPersonalListOfFolders().contains(folder)) {
                response.put("folder", folder.toFolderDto());
                response.put("isPosted", folder.getIsPosted());
                response.put("isShared", folder.getIsShared());


                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"); // Return 403 Forbidden if user doesn't have access to the folder
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
    }

    @GetMapping("/doc/{docId}")
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

    @GetMapping("share_folder/{folderId}")
    public ResponseEntity<?> shareFolder(@PathVariable long folderId) {
        try {
            Folder folder = folderRepository.getById(folderId);
            if (folder == null) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found if folder not found
            }
            folder.setIsShared(true);
            folderRepository.save(folder);
            Map<String, Object> response = new HashMap<>();

            if (folder.getIsShared()) {
                response.put("folder", folder.toFolderDto());
                response.put("isPosted", folder.getIsPosted());
                response.put("isShared", folder.getIsShared());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"); // Return 403 Forbidden if user doesn't have access to the folder
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
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

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }


}
