package com.github.camelya58.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class User represents a single entity in database.
 *
 * @author Kamila Meshcheryakova
 * created 28.08.2020
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@ToString
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
