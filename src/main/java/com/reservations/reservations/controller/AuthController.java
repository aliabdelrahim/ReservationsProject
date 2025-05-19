package com.reservations.reservations.controller;

import com.reservations.reservations.model.User;
import com.reservations.reservations.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; // correspond à templates/login.html
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") User user, Model model) {
        if (userService.loginExists(user.getLogin())) {
            model.addAttribute("user", user); // pour garder les données déjà saisies
            model.addAttribute("error", "Ce login est déjà utilisé. Veuillez en choisir un autre.");
            return "auth/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.addUser(user);
        return "redirect:/login?registerSuccess";
    }
}
