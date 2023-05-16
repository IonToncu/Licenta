package com.upt.easysign.model;


import com.upt.easysign.model.user.Notary;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;

@Entity
@Table(name = "registration_notary")
@Data
public class NotaryCandidate extends BaseEntity {
    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "prove_document")
    private byte[] proveDocument;

    public Notary toNotary(){
        Notary notary = new Notary();
        notary.setUsername(username);
        notary.setFirstName(firstName);
        notary.setLastName(lastName);
        notary.setEmail(email);
        notary.setPassword(password);
        notary.setUpdated(new Date());
        notary.setCreated(new Date());
        notary.setStatus(Status.ACTIVE);
        notary.setPersonalListOfFolders(new ArrayList<>());
        return notary;
    }
}
