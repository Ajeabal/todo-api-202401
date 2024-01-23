package com.study.todoapi.todo.dto.request;


import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoCheckRequestDTO {

    private String id;
    private boolean done;
}