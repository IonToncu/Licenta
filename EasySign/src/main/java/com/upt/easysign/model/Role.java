package com.upt.easysign.model;

import com.upt.easysign.model.user.Customer;
import com.upt.easysign.model.user.Notary;
import lombok.Data;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "roles")
@Data
public class Role extends BaseEntity {

    @Column(name = "name")
    private String name;

    @ManyToMany
    private List<Notary> notaryList;

    @ManyToMany
    private List<Customer> customersList;

    @Override
    public String toString() {
        return "Role{" +
                "id: " + super.getId() + ", " +
                "name: " + name + "}";
    }
}
