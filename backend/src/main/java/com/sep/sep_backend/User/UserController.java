package com.sep.sep_backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).build();
    }
}