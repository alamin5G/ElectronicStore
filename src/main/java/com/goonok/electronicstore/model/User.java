package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Transactional
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty( message = "username is required")
    @Size(min = 3, message = " username can't less than 3 characters")
    private String username;

    //@Convert(converter = PasswordEncoder.class)
    @NotEmpty(message = "password is required")
    @Size(min = 6, message = "Password must be at least 6 character")
    private String password;

    @NotEmpty(message = "Name is required")
    @Size(min = 3, message = "at least 3 character to be a name")
    private String fullName;

    @NotEmpty(message = "email is required")
    @Email
    private String email;

    @NotEmpty(message = "phone is required")
    @Size(min = 11, max = 11, message = "Phone number can't be less or more than 11 digit")
    @Pattern(regexp = "^\\+?[0-9. ()-]{11}$", message = "Phone number is invalid")
    private String phone;

    private boolean enabled;
    private boolean verified;

    //how to implement the address model in the user? will see later on

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles = new HashSet<>();


  /*  public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }*/

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                '}';
    }

}
