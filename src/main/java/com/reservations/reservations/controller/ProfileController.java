package com.reservations.reservations.controller;

import com.reservations.reservations.model.User;
import com.reservations.reservations.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String editProfile(Model model, Principal principal) {
        User user = userService.findByLogin(principal.getName());
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@ModelAttribute("user") User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        userService.updateUserProfile(principal.getName(), updatedUser);
        redirectAttributes.addFlashAttribute("message", "Profil mis à jour !");
        return "redirect:/profile";
    }
}
