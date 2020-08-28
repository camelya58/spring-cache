package com.github.camelya58.repository;

import com.github.camelya58.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface UserRepository sets up connection with database.
 *
 * @author Kamila Meshcheryakova
 * created 28.08.2020
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
