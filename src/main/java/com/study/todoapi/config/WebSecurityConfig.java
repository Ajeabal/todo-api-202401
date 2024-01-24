package com.study.todoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 시큐리티 기본 보안설정 해제
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors()
                .and()
                .csrf().disable()
                .httpBasic().disable()
                // 세션 인증은 더 이상 사용하지 않음
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 어떤 요청에서 인증을 하고 말지 설정
                .authorizeRequests() // 어떤 요청에서 인증을 할지
                .antMatchers("/", "/api/auth/**").permitAll() // 이 요청은 인증을 안해도 된다.
//                .antMatchers(HttpMethod.POST, "/api/todos").permitAll()
//                .antMatchers("/**").hasRole("ADMIN")
                .anyRequest().authenticated() // 나머지 요청은 모두 인증(로그인)받고 해라.
        ;

        return http.build();
    }

}