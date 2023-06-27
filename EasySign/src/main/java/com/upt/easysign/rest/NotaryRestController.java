package com.upt.easysign.rest;

import com.upt.easysign.dto.AuthenticationRequestDto;
import com.upt.easysign.dto.FolderDto;
import com.upt.easysign.dto.UserDto;
import com.upt.easysign.model.file.Document;
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

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.upt.easysign.certificate.NotaryCertificate.createKeyStore;
import static com.upt.easysign.model.file.FileStatus.CHECKED;
import static com.upt.easysign.model.file.FileStatus.DENIED;

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

    @GetMapping("notary/folder/{folderId}")
    public ResponseEntity<?> getFolder(@PathVariable long folderId){
        Folder folder = null;
        try {
            folder = stackFolderService.getByFolderId(folderId).getFolder();
        } catch (Exception e) {
            System.out.println(e);
            //ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }

        if (folder == null) {
            folder = notaryService.getNotaryFolderById(getCurrentUsername(), folderId);
            if (folder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found");
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("folder", folder.toFolderDto());
        return ResponseEntity.ok(response);

    }

    @GetMapping("notary/doc/{docId}")
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

    @GetMapping("notary/addToPersonal/{folderId}")
    public ResponseEntity<?> addToPersonal(@PathVariable long folderId) {
        try {
            Notary notary = notaryService.findByUsername(getCurrentUsername());
            StackFolder stackFolder = stackFolderService.getByFolderId(folderId);

            if (stackFolder == null) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found if stack folder not found
            }

            notary.addFolder(stackFolder.getFolder());
            notaryRepository.save(notary);
            stackFolderService.delete(stackFolder);

            Map<String, Object> response = new HashMap<>();
            response.put("notary", notary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
    }

    @GetMapping("notary/approve/doc")
    public ResponseEntity<?> approveDocument(@RequestParam("docId") long docId,
                                             @RequestParam("password") String password) {
        try {
            Document document = documentService.getById(docId);

            if (document == null) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found if document not found
            }
            if(document.getStatus() == CHECKED) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Document is already signed ");

            byte[] documentFile = document.getFile();
            Notary notary = notaryService.findByUsername(getCurrentUsername());
            byte[] signedFile = notary.signDocument(documentFile,
                    "Signed by " + notary.getFirstName() + " " + notary.getLastName(),
                    password
                    );
            document.setFile(signedFile);
            document.setStatus(CHECKED);
            document.setUpdated(new Date());
            documentService.save(document);

            Map<String, Object> response = new HashMap<>();
            response.put("response", "Signed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
    }

    @GetMapping("notary/decline/doc/{docId}")
    public ResponseEntity<?> declineDocument(@PathVariable long docId) {
        try {
            Document document = documentService.getById(docId);

            if (document == null) {return ResponseEntity.notFound().build(); }

            document.setStatus(DENIED);
            document.setUpdated(new Date());
            documentService.save(document);
            Map<String, Object> response = new HashMap<>();
            response.put("response", "Denied successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
    }

    @PostMapping("notary/approveDocument/")
    public ResponseEntity approveFolder(@RequestBody FolderDto requestDto) throws Exception {
        Folder folder = folderRepository.findById(requestDto.getId());
        folder.setStatus(requestDto.getFileStatus());
        folderRepository.save(folder);
        Map<String, Object> response = new HashMap<>();
        response.put("notary", folder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("notary/check_certificate")
    public ResponseEntity<?> checkCertificateIfExist() throws Exception {
        Notary notary = notaryRepository.findByUsername(getCurrentUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("hasCertificate", notary.getCertificate() != null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("notary/create_certificate")
    public ResponseEntity<?> createCertificate(@RequestParam("password") String password) throws Exception {
        Notary notary = notaryRepository.findByUsername(getCurrentUsername());
                byte[] keyStoreFile = createKeyStore(notary.getFirstName(),
                        notary.getLastName(),
                        password,
                        password);
        notary.setCertificate(keyStoreFile);
        notaryRepository.save(notary);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Certificate successfully created");
        System.out.println(response);
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
