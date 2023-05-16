package com.upt.easysign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upt.easysign.model.NotaryCandidate;
import com.upt.easysign.model.Status;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminNotaryCandidateDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String status;

//    private byte[] proveDocument;

    public NotaryCandidate toUser() {
        NotaryCandidate notaryCandidate = new NotaryCandidate();
        notaryCandidate.setId(id);
        notaryCandidate.setUsername(username);
        notaryCandidate.setFirstName(firstName);
        notaryCandidate.setLastName(lastName);
        notaryCandidate.setEmail(email);
        notaryCandidate.setStatus(Status.valueOf(status));
//        notaryCandidate.setProveDocument(proveDocument);
        return notaryCandidate;
    }

    public static AdminNotaryCandidateDto fromNotaryCandidate(NotaryCandidate notaryCandidate) {
        AdminNotaryCandidateDto adminNotaryCandidateDto = new AdminNotaryCandidateDto();
        adminNotaryCandidateDto.setId(notaryCandidate.getId());
//        adminNotaryCandidateDto.setProveDocument(notaryCandidate.getProveDocument());
        adminNotaryCandidateDto.setUsername(notaryCandidate.getUsername());
        adminNotaryCandidateDto.setFirstName(notaryCandidate.getFirstName());
        adminNotaryCandidateDto.setLastName(notaryCandidate.getLastName());
        adminNotaryCandidateDto.setEmail(notaryCandidate.getEmail());
        adminNotaryCandidateDto.setStatus(notaryCandidate.getStatus().name());
        return adminNotaryCandidateDto;
    }
}