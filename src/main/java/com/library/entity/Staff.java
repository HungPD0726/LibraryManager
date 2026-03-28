package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "roles")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StaffID")
    private Integer staffId;

    @Column(name = "StaffName", nullable = false, length = 100)
    private String staffName;

    @Column(name = "Username", length = 50, unique = true)
    private String username;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Password", length = 100)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "StaffRole",
        joinColumns = @JoinColumn(name = "StaffID"),
        inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private Set<Role> roles = new HashSet<>();
}
