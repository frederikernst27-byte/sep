package com.sep.sep_backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController // diese Klasse ist ein API-Controller
@RequestMapping("/users") // alle Endpoints starten mit /users
public class UserController {

    @Autowired
    private UserService userService; // bekommt userservice

    // Registrierung
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user); // gibt user an service weiter und speichert ihn
    }

    // Login
    @PostMapping("/login")
    public User login(@RequestParam String email, @RequestParam String password) { // nimmt passwort und email entgegen
        return userService.login(email, password); // service prüft obt login daten stimmen
    }
}