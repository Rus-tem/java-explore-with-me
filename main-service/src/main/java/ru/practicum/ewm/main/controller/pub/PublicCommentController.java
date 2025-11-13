package ru.practicum.ewm.main.controller.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.service.api.CommentService;

import java.util.List;

@RestController
@RequestMapping("/public/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    // Public. Получение списка Comment по Id Event
    @GetMapping("/{eventId}")
    public List<CommentDto> getPublicComment(@PathVariable Long eventId) {

        return commentService.getPublicComment(eventId);
    }
}

