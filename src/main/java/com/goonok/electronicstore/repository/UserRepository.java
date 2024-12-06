package com.goonok.electronicstore.repository;


import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
    public Optional<User> findByPhone(String phone);
    public List<User> findByRoles_Name(String roleName);

    //similar method to find user list by role
    public List<User> findByRolesContaining(Role role);


    public long countByEnabled(boolean enabled);
    public List<User> findByEnabledTrue();
    public List<User> findByEnabledFalse();
    public List<User> findByVerifiedFalse();

    public List<User> findByEnabled(boolean enabled);

    @Query("SELECT u.email FROM User u WHERE u.username = :username")
    String getEmailByUsername(@Param("username") String username);

    @Query("SELECT u.phone FROM User u WHERE u.username = :username")
    String getPhoneByUsername(@Param("username") String username);



}
