package com.sep.sep_backend.User;

import org.springframework.data.jpa.repository.JpaRepository; // fertige Datenbank funktionen (zb.: save (), findById())

public interface UserRepository extends JpaRepository<User, Long> { // arbeitet mit User objekten

    User findByEmail(String email); // sucht User anhand E-Mail
}