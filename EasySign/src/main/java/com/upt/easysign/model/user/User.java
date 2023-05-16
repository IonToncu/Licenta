package com.upt.easysign.model.user;

import com.upt.easysign.model.BaseEntity;
import com.upt.easysign.model.Role;
import com.upt.easysign.model.file.Folder;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@Data
public class User extends BaseEntity {

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

    @OneToMany
    private List<Folder> personalListOfFolders;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    public void addFolder(Folder folder){
        if(personalListOfFolders == null) personalListOfFolders = new ArrayList<>();
        personalListOfFolders.add(folder);
    }

}
