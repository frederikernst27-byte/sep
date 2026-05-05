package com.sep.sep_backend.User;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // erkennt User als Datenbank Tabelle
@Table(name = "users")
public class User {
    @Id // id primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id wird automatisch erzeugt
    private Long id;
    private String email;
    private String password;

    public User() {}

    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
