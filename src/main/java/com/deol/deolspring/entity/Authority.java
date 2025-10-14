package com.deol.deolspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_authority")
public class Authority {

    @Id
    @Column(name = "authority_Seq", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer authoritySeq;

    @Column(name = "authorityName", length = 50)
    private String authorityName;

}