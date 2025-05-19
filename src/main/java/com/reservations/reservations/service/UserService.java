package com.reservations.reservations.service;


import com.reservations.reservations.model.User;
import com.reservations.reservations.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        repository.findAll().forEach(users::add);

        return users;
    }

    public User getUser(String id) {
        int indice = Integer.parseInt(id);

        return repository.findById(indice);
    }

    public void addUser(User user) {
        repository.save(user);
    }

    public void updateUser(String id, User user) {
        repository.save(user);
    }

    public void updateUserProfile(String login, User updatedData) {
        User existingUser = repository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // On ne modifie que les champs autorisés
        existingUser.setFirstname(updatedData.getFirstname());
        existingUser.setLastname(updatedData.getLastname());
        existingUser.setEmail(updatedData.getEmail());
        existingUser.setLangue(updatedData.getLangue());

        repository.save(existingUser);
    }

    public void deleteUser(String id) {
        Long indice = (long) Integer.parseInt(id);

        repository.deleteById(indice);
    }

    public User findByLogin(String login) {
        return repository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }

    public boolean loginExists(String login) {
        return repository.findByLogin(login).isPresent();
    }
}
