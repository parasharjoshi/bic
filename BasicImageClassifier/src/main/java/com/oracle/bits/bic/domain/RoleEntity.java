/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
@Entity
@Table(name = "ROLE")
@SequenceGenerator(name = "RoleIdGenerator", sequenceName = "ROLE_ID_SEQ", allocationSize = 1)
public class RoleEntity extends BaseEntity implements Serializable {

    private Long id;
    private String roleName;
    private String roleDesc;
    private String roleKey;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RoleIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "role_name", length = 255, nullable = false)
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Column(name = "role_desc", length = 2000, nullable = false)
    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    @Column(name = "role_key", length = 255, nullable = false)
    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }
}
