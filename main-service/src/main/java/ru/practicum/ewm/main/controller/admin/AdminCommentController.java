package ru.practicum.ewm.main.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.service.api.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    // Admin. Получение списка Comment по параметрам
    @GetMapping()
    public List<CommentDto> getAdminComment(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Long eventId,
                                            @RequestParam(required = false) Long commentId,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        return commentService.getAdminComment(userId, eventId, commentId, rangeStart, rangeEnd, from, size);
    }

    // Admin. Удаление Comment любого Event
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdminComment(@PathVariable Long eventId,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) Long commentId) {
        commentService.deleteAdminComment(eventId, userId, commentId);

    }

}
