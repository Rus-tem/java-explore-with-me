package ru.practicum.ewm.main.service.api;

import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentRequest;

import java.util.List;

public interface CommentService {

    // Admin. Получение списка Comment по параметрам
    List<CommentDto> getAdminComment(Long userId, Long eventId, Long commentId, String rangeStart, String rangeEnd, int from, int size);

    // Admin. Удаление Comment любого Event
    void deleteAdminComment(Long eventId, Long userId, Long commentId);

    // Public. Получение списка Comment по Id
    List<CommentDto> getPublicComment(Long eventId);

    //Private. Создание Comment
    CommentDto createPrivateComment(Long userId, Long eventId, CommentRequest commentRequest);

    //Private. Изменение Comment
    CommentDto updatePrivateComment(Long userId, Long eventId, Long commentId, CommentRequest commentRequest);

    //Private. Удаление Comment
    void deletePrivateComment(Long userId, Long eventId, Long commentId);

    //Private. Получение списка Comment по параметрам
    List<CommentDto> getPrivateComment(Long userId, Long eventId, Long commentId);
}
