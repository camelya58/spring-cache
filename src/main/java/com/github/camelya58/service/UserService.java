package com.github.camelya58.service;

import com.github.camelya58.model.User;

import java.util.List;

/**
 * Interface UserService configures how to work with users.
 *
 * @author Kamila Meshcheryakova
 * created 28.08.2020
 */
public interface UserService {

    User create(User user);
    User create(String name, String email);
    User get(Long id);
    List<User> getAll();
    User createOrReturnCached(User user);
    User createAndRefreshCache(User user);
    void delete(Long id);
    void deleteAndEvict(Long id);
}
