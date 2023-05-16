package com.upt.easysign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upt.easysign.model.Status;
import com.upt.easysign.model.user.Customer;
import com.upt.easysign.model.user.Notary;
import com.upt.easysign.model.user.User;
import lombok.Data;

import java.util.Date;

/**
 * DTO class for user requests by ROLE_USER
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
//    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    public User toUser(){
        User user = new User();
//        user.setId(id);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    public Customer toCustomer(){
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPassword(password);
        customer.setCreated(new Date());
        customer.setUpdated(new Date());
        customer.setStatus(Status.ACTIVE);
        return customer;
    }

    public Notary toNotary(){
        Notary notary = new Notary();
        notary.setUsername(username);
        notary.setFirstName(firstName);
        notary.setLastName(lastName);
        notary.setEmail(email);
        notary.setPassword(password);
        notary.setCreated(new Date());
        notary.setUpdated(new Date());
        notary.setStatus(Status.ACTIVE);
        return notary;
    }

    public static UserDto fromUser(User user) {
        UserDto userDto = new UserDto();
//        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }
}
