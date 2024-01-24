package com.study.todoapi.auth;

import com.study.todoapi.user.entity.Role;
import lombok.*;

import javax.persistence.Entity;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TokenUserInfo {

    private String userId;
    private String email;
    private Role role;
}
