package com.sep.sep_backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // makiert als Service Klasse
public class UserService {

    @Autowired
    private UserRepository userRepository; // verbindet service und repository

    // Registrierung
    public User register(User user) { // registrierungs Methode
        return userRepository.save(user); // speichert User in DB
    }

    // Login
    public User login(String email, String password) { // login Methode
        User user = userRepository.findByEmail(email); // sucht in DB user mit der mail

        if (user != null && user.getPassword().equals(password)) { // gibt es user ? stimmt passwort ?
            return user; // login erfolgreich
        }

        return null; // login fehlgeschlagen
    }
}