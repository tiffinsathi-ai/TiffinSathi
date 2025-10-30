package com.tiffin_sathi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ --- Implementation of UserDetails interface methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Map user role to a GrantedAuthority
        return List.of(() -> "ROLE_" + role.name());
    }

    @Override
    public String getUsername() {
        // Use email as the username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can customize with an "accountExpired" field later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can customize with an "accountLocked" field later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can customize with a "credentialsExpired" field
    }

    @Override
    public boolean isEnabled() {
        return true; // You can add a "boolean active" field to control this
    }
}
