package com.smartcampus.events.controller;

import com.smartcampus.events.dto.UserRegistrationDto;
import com.smartcampus.events.model.Department;
import com.smartcampus.events.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("errorMsg", "Invalid email or password. Please try again.");
        if (logout != null)
            model.addAttribute("logoutMsg", "You have been logged out successfully.");
        return "login";
    }

    /* ===== STUDENT REGISTRATION ===== */
    @GetMapping("/register/student")
    public String studentRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("departments", Department.values());
        model.addAttribute("userType", "Student");
        return "register/student";
    }

    @PostMapping("/register/student")
    public String registerStudent(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {
        model.addAttribute("departments", Department.values());
        model.addAttribute("userType", "Student");

        if (result.hasErrors())
            return "register/student";

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            model.addAttribute("usernameError", "Username is required.");
            return "register/student";
        }
        if (userService.usernameExists(dto.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken.");
            return "register/student";
        }

        if (!dto.isPasswordMatching()) {
            model.addAttribute("passwordError", "Passwords do not match.");
            return "register/student";
        }
        if (userService.emailExists(dto.getEmail())) {
            model.addAttribute("emailError", "Email is already registered.");
            return "register/student";
        }

        userService.registerStudent(dto);
        redirectAttrs.addFlashAttribute("successMsg", "Account created! Please login.");
        return "redirect:/login";
    }

    /* ===== ADMIN REGISTRATION ===== */
    @GetMapping("/register/admin")
    public String adminRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("departments", Department.values());
        model.addAttribute("userType", "Admin");
        return "register/admin";
    }

    @PostMapping("/register/admin")
    public String registerAdmin(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {
        model.addAttribute("departments", Department.values());
        model.addAttribute("userType", "Admin");

        if (result.hasErrors())
            return "register/admin";

        // Username is optional for admins (student registration requires it).
        if (dto.getUsername() != null && !dto.getUsername().isBlank() && userService.usernameExists(dto.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken.");
            return "register/admin";
        }

        if (!dto.isPasswordMatching()) {
            model.addAttribute("passwordError", "Passwords do not match.");
            return "register/admin";
        }
        if (userService.emailExists(dto.getEmail())) {
            model.addAttribute("emailError", "Email is already registered.");
            return "register/admin";
        }

        userService.registerAdmin(dto);
        redirectAttrs.addFlashAttribute("successMsg", "Admin account created! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", "You do not have permission to access this page.");
        return "error";
    }
}
