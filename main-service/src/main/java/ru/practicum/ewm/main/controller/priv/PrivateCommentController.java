package ru.practicum.ewm.main.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentRequest;
import ru.practicum.ewm.main.service.api.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    //Private. Создание Comment
    @PostMapping("/{userId}/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createPrivateComment(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @Valid @RequestBody CommentRequest commentRequest) {
        return commentService.createPrivateComment(userId, eventId, commentRequest);
    }

    //Private. Изменение Comment
    @PatchMapping("/{userId}/{eventId}/{commentId}")
    public CommentDto updatePrivateComment(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @PathVariable Long commentId,
                                           @Valid @RequestBody CommentRequest commentRequest) {

        return commentService.updatePrivateComment(userId, eventId, commentId, commentRequest);
    }

    //Private. Удаление Comment
    @DeleteMapping("/{userId}/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePrivateComment(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @RequestParam(required = false) Long commentId) {
        commentService.deletePrivateComment(userId, eventId, commentId);

    }

    //Private. Получение списка Comment по параметрам
    @GetMapping()
    public List<CommentDto> getPrivateComment(@RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) Long eventId,
                                              @RequestParam(required = false) Long commentId) {

        return commentService.getPrivateComment(userId, eventId, commentId);
    }

}



