package com.jccv.tuprivadaapp.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "users")
@JsonIgnoreProperties(value = {"condominium"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    @NotBlank(message = "El nombre no puede estar vacío")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "El apellido no puede estar vacío")
    private String lastName;

    @Column(name = "username", unique = true)
    @NotBlank(message = "El username no puede estar vacío")
    private String username;

    @Column(name = "password")
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    @Column(name = "email", unique = true)
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @Enumerated(value = EnumType.STRING)
    @Valid()
    private Role role;


    private String bankPersonalReference;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "condominium_id")
    private Condominium condominium;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference  // Este lado se serializa
    @ToString.Exclude
    private List<Token> tokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}