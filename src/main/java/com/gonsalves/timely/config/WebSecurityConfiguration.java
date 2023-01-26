package com.gonsalves.timely.config;



import com.gonsalves.timely.util.LogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Profile({"dev", "prod"})
public class WebSecurityConfiguration {

    private final LogoutHandler logoutHandler;

    @Autowired
    WebSecurityConfiguration(LogoutHandler logoutHandler) {this.logoutHandler = logoutHandler;}
    private static final String[] WHITE_LIST_URLS = {"/", "/home","/product", "/login"};
    private static final String[] CSRF_WHITE_LIST_URLS = {"/api/v1/project/**", "/api/v1/task/**","/api/v1/project/**/**", "/api/v1/task/**/**"};


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
    @Bean
    @Profile({"dev","prod"})
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/images/**", "/css/**","/api/v1/project/**/**", "/api/v1/task/**/**","/api/v1/project/**/**","/api/v1/task/**/**").permitAll();
        http.csrf().ignoringAntMatchers(CSRF_WHITE_LIST_URLS);
        return http
                .authorizeRequests()
                .mvcMatchers(WHITE_LIST_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().defaultSuccessUrl("/dashboard")
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler(logoutHandler)
                .and().build();

    }
}
