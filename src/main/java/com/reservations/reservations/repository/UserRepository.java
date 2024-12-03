package com.reservations.reservations.repository;

import com.reservations.reservations.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface UserRepository extends CrudRepository<User,Long> {
    User findByLogin(String login);
    List<User> findByLastname(String lastname);

    User findById(long id);
}

