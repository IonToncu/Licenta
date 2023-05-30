package com.upt.easysign.rest;

import com.upt.easysign.dto.FolderDto;
import com.upt.easysign.model.file.Document;
import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.user.Customer;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping(value = "/api/v1/utility")
@CrossOrigin
public class BasicController {
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

    public BasicController(CustomerService customerService, FolderService folderService, DocumentService documentService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, CustomerRepository customerRepository, FolderRepository folderRepository, PublicStackFolderService stackFolderService, NotaryService notaryService, NotaryRepository notaryRepository) {
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

    @GetMapping("shared_folder/{folderId}")
    public ResponseEntity<?> shareFolder(@PathVariable long folderId) {
        try {
            Folder folder = folderRepository.getById(folderId);
            if (folder == null) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found if folder not found
            }

            Map<String, Object> response = new HashMap<>();

            if (folder.getIsShared()) {
                response.put("folder", folder.toFolderDto());
                response.put("isPosted", folder.getIsPosted());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"); // Return 403 Forbidden if user doesn't have access to the folder
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred"); // Return 500 Internal Server Error for any other exceptions
        }
    }
    @GetMapping("/doc")
    @ResponseBody
    public ResponseEntity<byte[]> getSharedDocumentOfCandidate(@RequestParam("folderId") long folderId,
                                                               @RequestParam("docId") long docId) throws IOException {

        Folder folder = folderService.findById(folderId);
        if(!folder.getIsShared())  return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied".getBytes());

        Document document = documentService.getById(docId);
        if(!folder.getDocuments().contains(document)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied".getBytes());
        byte[] bytes = document.getFile();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        String filename = document.getFileName();
        headers.add("content-disposition", "inline;filename=" + filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
        return response;
    }
}
