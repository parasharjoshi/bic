/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
import javax.persistence.CascadeType;
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
@Table(name = "ACCOUNT")
@SequenceGenerator(name = "AccountIdGenerator", sequenceName = "ACCOUNT_ID_SEQ", allocationSize = 1)
public class AccountEntity extends BaseEntity implements Serializable {

    private Long id;
    private String userName;
    private String password;
    private PersonEntity user;
    private Boolean active;
    private Boolean locked;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccountIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "user_name", length = 50, nullable = false)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "password", length = 50, nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    public PersonEntity getUser() {
        return user;
    }

    public void setUser(PersonEntity user) {
        this.user = user;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Override
    public void prePersist() {
        if (active == null) {
            active = true;
        }
        if (locked == null) {
            locked = false;
        }
    }
}
