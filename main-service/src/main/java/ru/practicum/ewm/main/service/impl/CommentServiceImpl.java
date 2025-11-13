package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentRequest;
import ru.practicum.ewm.main.exception.CommentNotFoundException;
import ru.practicum.ewm.main.exception.CommentValidationException;
import ru.practicum.ewm.main.exception.EventNotFoundException;
import ru.practicum.ewm.main.exception.UserNotFoundException;
import ru.practicum.ewm.main.mapper.CommentMapper;
import ru.practicum.ewm.main.model.Comment;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.CommentRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.api.CommentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Admin. Получение списка Comment по параметрам
    @Override
    public List<CommentDto> getAdminComment(Long userId, Long eventId, Long commentId, String rangeStart, String rangeEnd, int from, int size) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime rangeStartFormatted;
        LocalDateTime rangeEndFormatted;

        if (rangeStart == null || rangeEnd.isBlank()) {
            rangeStartFormatted = null;
        } else {
            rangeStartFormatted = LocalDateTime.parse(rangeStart, formatter);
        }
        if (rangeEnd == null || rangeEnd.isBlank()) {
            rangeEndFormatted = null;
        } else {
            rangeEndFormatted = LocalDateTime.parse(rangeEnd, formatter);
        }

        int page = from / size;
        Pageable pageRequest = PageRequest.of(from / size, size);
        Page<Comment> commentPage;
        if (rangeStart == null && rangeEnd == null) {
            commentPage = commentRepository.findAllByAdminParams(
                    userId,
                    eventId,
                    commentId,
                    pageRequest
            );

        } else {
            commentPage = commentRepository.findAllByAdminAllParams(
                    userId,
                    eventId,
                    commentId,
                    rangeStartFormatted,
                    rangeEndFormatted,
                    pageRequest
            );
        }

        return commentPage.getContent().stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();

    }

    // Admin. Удаление Comment любого Event
    @Override
    public void deleteAdminComment(Long eventId, Long userId, Long commentId) {

        eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException("Event с таким id = " + eventId + " не найден"));

        if (userId != null) {
            userRepository.findById(userId).orElseThrow(() ->
                    new UserNotFoundException("User с таким id = " + userId + " не найден"));
            commentRepository.deleteAllByEventIdAndUserId(eventId, userId);
        } else if (commentId != null) {
            commentRepository.findById(commentId).orElseThrow(() ->
                    new CommentNotFoundException("Comment с таким id = " + commentId + " не найден"));
            commentRepository.deleteByIdAndEventId(commentId, eventId);
        } else {
            commentRepository.deleteByEvent(eventId);
        }
    }

    // Public. Получение списка Comment по Id
    @Override
    public List<CommentDto> getPublicComment(Long eventId) {

        eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException("Event с таким id = " + eventId + " не найден"));

        return commentRepository.findAllByEventId(eventId)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    //Private. Создание Comment
    @Override
    public CommentDto createPrivateComment(Long userId, Long eventId, CommentRequest commentRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException("Event с таким id = " + eventId + " не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User с таким id = " + userId + "не найден"));

        if (event.getState().equals(State.PUBLISHED)) {
            Comment comment = new Comment();
            comment.setComment(commentRequest.getComment());
            comment.setEvent(event);
            comment.setUser(user);
            comment.setCreatedOn(LocalDateTime.now());
            commentRepository.save(comment);

            return CommentMapper.mapToCommentDto(comment);
        } else {
            throw new CommentValidationException("Добавлять Comment можно только к опубликованным Event");
        }
    }

    //Private. Изменение Comment
    @Override
    public CommentDto updatePrivateComment(Long userId, Long eventId, Long commentId, CommentRequest commentRequest) {

        eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException("Event с таким id = " + eventId + " не найден"));

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User с таким id = " + userId + "не найден"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment с таким id = " + commentId + " не найден"));

        if (comment.getEvent().getId().equals(eventId) && comment.getUser().getId().equals(userId)) {
            comment.setComment(commentRequest.getComment());
            commentRepository.save(comment);
            return CommentMapper.mapToCommentDto(comment);
        } else {
            throw new CommentValidationException("Переданы неправильные Id Event или User ");
        }
    }

    //Private. Удаление Comment
    @Override
    public void deletePrivateComment(Long userId, Long eventId, Long commentId) {

        eventRepository.findById(eventId).orElseThrow(() ->
                new EventNotFoundException("Event с таким id = " + eventId + " не найден"));

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User с таким id = " + userId + "не найден"));
        if (commentId != null) {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new CommentNotFoundException("Comment с таким id = " + commentId + " не найден"));

            if (comment.getEvent().getId().equals(eventId) && comment.getUser().getId().equals(userId)) {
                commentRepository.delete(comment);
            } else {
                throw new CommentValidationException("Переданы неправильные Id Event или User");
            }
        } else {
            commentRepository.deleteAllByUserIdAndEventId(userId, eventId);
        }
    }

    //Private. Получение списка Comment по параметрам
    @Override
    public List<CommentDto> getPrivateComment(Long userId, Long eventId, Long commentId) {

        List<Comment> comments = commentRepository.findAllByParams(
                userId,
                eventId,
                commentId
        );

        return comments.stream().map(CommentMapper::mapToCommentDto).toList();
    }
}
