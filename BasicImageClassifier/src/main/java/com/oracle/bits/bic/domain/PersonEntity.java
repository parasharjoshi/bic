/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@Entity
@Table(name = "PERSON")
@SequenceGenerator(name = "PersonIdGenerator", sequenceName = "PERSON_ID_SEQ", allocationSize = 1)
public class PersonEntity extends BaseEntity implements Serializable {

    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String emailId;
    private RoleEntity role;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "first_name", length = 50, nullable = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "last_name", length = 50, nullable = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "user_name", length = 50, nullable = false)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "email_id", length = 100, nullable = false)
    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

}
