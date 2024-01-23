package com.study.todoapi.todo.service;

import com.study.todoapi.todo.dto.request.TodoCheckRequestDTO;
import com.study.todoapi.todo.dto.request.TodoCreateRequestDTO;
import com.study.todoapi.todo.dto.response.TodoDetailResponseDTO;
import com.study.todoapi.todo.dto.response.TodoListResponseDTO;
import com.study.todoapi.todo.entity.Todo;
import com.study.todoapi.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional  // JPA 사용시 필수
public class TodoService {

    private final TodoRepository todoRepository;

    // 할 일 등록
    public TodoListResponseDTO create(TodoCreateRequestDTO dto) {
        todoRepository.save(dto.toEntity());
        log.info("새로운 할 일이 저장되었습니다. 제목: {}", dto.getTitle());

        return retrieve();
    }


    // 할 일 목록 불러오기
    public TodoListResponseDTO retrieve() {

        List<Todo> todoList = todoRepository.findAll();

        // 엔터티 리스트를 DTO리스트로 매핑
        List<TodoDetailResponseDTO> dtoList = todoList.stream()
                .map(TodoDetailResponseDTO::new)
                .collect(Collectors.toList());

        return TodoListResponseDTO.builder()
                .todos(dtoList)
                .build();
    }

    // 할 일 삭제
    public TodoListResponseDTO delete(String id) {

        try {
            todoRepository.deleteById(id);
        } catch (Exception e) {
            log.error("id가 존재하지 않아 삭제에 실패했습니다. - ID: {}, error: {}",
                    id, e.getMessage());
            throw new RuntimeException("삭제에 실패했습니다!!");
        }
        return retrieve();
    }

    // 할 일 체크 처리
    public TodoListResponseDTO check(TodoCheckRequestDTO dto) {

        Optional<Todo> target = todoRepository.findById(dto.getId());

        target.ifPresent(todo -> {
            todo.setDone(dto.isDone());
            todoRepository.save(todo);
        });

        return retrieve();
    }

}