package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    void todo가_정상적으로_등록된다() {
        // given
        String weather = "Sunny";
        given(weatherClient.getTodayWeather()).willReturn(weather);

        User user = new User("user@example.com", "password", UserRole.USER);
        Todo newTodo = new Todo("Title", "Contents", weather, user);
        given(todoRepository.save(any(Todo.class))).willReturn(newTodo);

        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ADMIN);
        TodoSaveRequest request = new TodoSaveRequest("Title", "Contents");

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        // then
        assertEquals("Title", response.getTitle());
        assertEquals(weather, response.getWeather());
    }

    @Test
    void todo_목록_조회에_성공한다() {
        // given
        User user = new User("user@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Page<Todo> todosPage = new PageImpl<>(List.of(todo));

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todosPage);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<TodoResponse> todoResponses = todoService.getTodos(1, 10);

        // then
        assertEquals(1,todoResponses.getTotalElements());
        assertEquals("Title", todoResponses.getContent().get(0).getTitle());
    }

    @Test
    void todo_조회_성공한다() {
        // given
        User user = new User("user@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        given(todoRepository.findByIdWithUser(any(Long.class))).willReturn(Optional.of(todo));

        // when
        TodoResponse todoResponse = todoService.getTodo(1L);

        // then
        assertEquals("Title", todoResponse.getTitle());
        assertEquals("Sunny", todoResponse.getWeather());
    }

    @Test
    void todo가_존재하지_않으면_예외가_발생한다() {
        // given
        given(todoRepository.findByIdWithUser(any(Long.class))).willReturn(Optional.empty());

        // when & then
        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            todoService.getTodo(1L);
        });

        assertEquals("Todo not found", thrownException.getMessage());
    }
}