package com.gonsalves.TimeManagementSystem.controller;

import com.gonsalves.TimeManagementSystem.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class WebController {
    @Autowired
    ProjectService projectService;


    @RequestMapping({"/home", "/", "/product"})
    public String home(Authentication principal) {
        return "product";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "product";
    }

}
