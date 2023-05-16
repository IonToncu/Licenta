package com.upt.easysign.service;

import com.upt.easysign.model.NotaryCandidate;

import java.util.List;

public interface NotaryQueueService {
    NotaryCandidate addAsCandidate(NotaryCandidate notaryCandidate);
    List<NotaryCandidate> getAll();
    Boolean deleteCandidate(NotaryCandidate notaryCandidate);
    Boolean addCandidateToNotary(NotaryCandidate notaryCandidate);
    NotaryCandidate getNotaryCandidateByEmail(String email);
    NotaryCandidate getNotaryCandidateById(long id);
}
