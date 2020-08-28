package com.github.camelya58.service;

import com.github.camelya58.model.User;
import com.github.camelya58.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Class UserServiceImpl represents a realization of UserService interface.
 *
 * @author Kamila Meshcheryakova
 * created 28.08.2020
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    @Cacheable("users")
    public User get(Long id) {
        log.info("getting user by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id " + id));
    }
    @Override
    @Cacheable(value = "users", key = "#name")
    public User create(String name, String email) {
        log.info("creating user with parameters: {}, {}", name, email);
        return repository.save(new User(name, email));
    }

    @Override
    @Cacheable(value = "users", key = "#user.name")
    public User createOrReturnCached(User user) {
        log.info("creating user: {}", user);
        return repository.save(user);
    }

    @Override
    @CachePut(value = "users", key = "#user.name")
    public User createAndRefreshCache(User user) {
        log.info("creating user: {}", user);
        return repository.save(user);
    }

    @Override
    public void delete(Long id) {
        log.info("deleting user by id: {}", id);
        repository.deleteById(id);
    }

    @Override
    @CacheEvict("users")
    public void deleteAndEvict(Long id) {
        log.info("deleting user by id: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }
}
