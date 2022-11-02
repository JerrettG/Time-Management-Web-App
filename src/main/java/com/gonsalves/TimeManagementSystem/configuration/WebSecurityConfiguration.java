package com.gonsalves.TimeManagementSystem.configuration;

import com.gonsalves.TimeManagementSystem.controller.LogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Autowired
    private LogoutHandler logoutHandler;
    private static final String[] WHITE_LIST_URLS = {"/", "/home","/product", "/login"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/images/**", "/css/**").permitAll();
        return http
                .authorizeRequests()
                .mvcMatchers(WHITE_LIST_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().defaultSuccessUrl("/projects/")
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutHandler)
                .and().build();

    }
}
