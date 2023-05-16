package com.upt.easysign.model.user;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "notary")
@Data
public class Notary extends User{
    @ToString.Exclude
    @Column(name = "file")
    private byte[] certificate;
}
