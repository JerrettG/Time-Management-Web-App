package com.gonsalves.timely.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {



    @RequestMapping("/")
    public String index() {
        return "product";
    }
    @RequestMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OidcUser principal, Model model) {
        String userId = principal.getPreferredUsername();
        model.addAttribute("userId", userId);
        return "index";
    }

    @RequestMapping("/project/{projectName}")
    public String project(@AuthenticationPrincipal OidcUser principal, Model model) {
        String userId = principal.getPreferredUsername();
        model.addAttribute("userId", userId);
        return "project";
    }
}
