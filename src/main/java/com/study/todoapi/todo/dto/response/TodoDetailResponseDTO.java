package com.study.todoapi.todo.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.todoapi.todo.entity.Todo;
import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoDetailResponseDTO {

    private String id;

    //    @JsonProperty("todo-title")
    private String title;

    private boolean done;

    // 엔터티를 dto로 바꿔주는 생성자
    public TodoDetailResponseDTO(Todo todo) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.done = todo.isDone();
    }

}