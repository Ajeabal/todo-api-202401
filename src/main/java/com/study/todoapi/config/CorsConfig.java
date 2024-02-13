package com.study.todoapi.config;

// 전역 크로스오리진 설정 : 클라이언트 허용 아이피 설정

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
                .addMapping("/api/**") // 어떤 요청 유알엘을 허용할지
                .allowedOrigins("http://localhost:3000", "http://todo-react-ver2.s3-website.ap-northeast-2.amazonaws.com") // 어떤 클라이언트를 허용할지
                .allowedMethods("*") // 어떤 요청방식을 허용할지
                .allowedHeaders("*") // 어떤 헤더를 허용할지
                .allowCredentials(true) // 쿠키 전달을 허용할지
                .maxAge(3600) // 허용시간에 대한 캐싱 설정
        ;

    }
}
