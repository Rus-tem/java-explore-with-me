package ru.practicum.ewm.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.model.Comment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setComment(comment.getComment());
        commentDto.setEvent(EventMapper.eventMapToEventShortDto(comment.getEvent()));
        commentDto.setUser(UserMapper.mapToUserDto(comment.getUser()));
        commentDto.setCreatedOn(comment.getCreatedOn());
        return commentDto;
    }

}
