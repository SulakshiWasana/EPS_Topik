package com.sejong.academy.eps_topik.entities;

import com.sejong.academy.eps_topik.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_user_sequence")
    @SequenceGenerator(name = "admin_user_sequence", sequenceName = "admin_user_sequence", allocationSize = 1)
    private Long id;

    @Builder.Default
    private String username = UUID.randomUUID().toString();
    private String name;
    private String password;
    private String nic;
    @Builder.Default
    private String status = Status.PENDING.name();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_users_roles",
            joinColumns = @JoinColumn(
                    name = "admin_user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))

    private Collection<Role> roles = new ArrayList<>();

}


