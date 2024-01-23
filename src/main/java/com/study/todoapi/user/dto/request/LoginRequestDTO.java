package com.study.todoapi.user.dto.request;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {
    private String id; // account가 아니라 랜덤식별번호

    private String email;

    private String password;

    private String userName;

    private LocalDateTime joinDate;
}
