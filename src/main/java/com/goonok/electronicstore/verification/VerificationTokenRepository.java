package com.goonok.electronicstore.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserId(int userId);
    int findUserIdByToken(String token);
    void deleteByToken(String token);




}

