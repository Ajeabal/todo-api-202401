package com.study.todoapi.user.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.todoapi.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignUpResponseDTO {

    private String email;
    private String userName;

    @JsonProperty("join-date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;


    // 엔터티를 DTO로 변경하는 생성자
    public UserSignUpResponseDTO(User user) {
        this.email = user.getEmail();
        this.userName = user.getUserName();
        this.joinDate = user.getJoinDate();
    }
}