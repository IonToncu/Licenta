package com.upt.easysign.model.user;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "customer")
@Data
public class Customer extends User{

}
