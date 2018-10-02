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
@Table(name = "TRAINING_LABELS")
@SequenceGenerator(name = "TrainLabelIdGenerator", sequenceName = "TRAINING_LABELS_ID_SEQ", allocationSize = 1)
public class TrainingLabelEntity extends BaseEntity implements Serializable {

    private Long id;
    private String label;
    private String trained;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TrainLabelIdGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "LABEL", nullable = false, length = 1000)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "TRAINED", nullable = false, length = 2)
    public String getTrained() {
        return trained;
    }

    public void setTrained(String trained) {
        this.trained = trained;
    }
}
