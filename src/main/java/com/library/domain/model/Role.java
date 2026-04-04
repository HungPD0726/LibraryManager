package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Role")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleId;

    @Column(name = "RoleName", nullable = false, length = 50, unique = true)
    private String roleName;
}
