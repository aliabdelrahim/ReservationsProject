package com.reservations.reservations.repository;

import com.reservations.reservations.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends CrudRepository<User,Long> {
    List<User> findByLastname(String lastname);

    User findById(long id);;
    User findByEmail(String email);
    Optional<User> findByLogin(String login);
}

