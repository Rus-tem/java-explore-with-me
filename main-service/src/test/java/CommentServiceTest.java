import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentRequest;
import ru.practicum.ewm.main.exception.CommentValidationException;
import ru.practicum.ewm.main.mapper.CommentMapper;
import ru.practicum.ewm.main.model.Comment;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.CommentRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.impl.CommentServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Event event;
    private User user;
    private Comment comment;
    private CommentDto commentDto;
    private CommentRequest request;

    @BeforeEach
    void setUp() {
        event = new Event(1L, State.PUBLISHED);
        user = new User(1L, "John", "john@example.com");
        comment = new Comment(1L, "Nice event!", event, user, LocalDateTime.now());
        request = new CommentRequest("Updated comment");
        commentDto = new CommentDto(1L, "Nice event!", null, null, comment.getCreatedOn());
    }

    @Test
    void createPrivateComment_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        try (MockedStatic<CommentMapper> mockedMapper = mockStatic(CommentMapper.class)) {
            mockedMapper.when(() -> CommentMapper.mapToCommentDto(any(Comment.class)))
                    .thenReturn(commentDto);

            CommentDto result = commentService.createPrivateComment(1L, 1L, request);

            assertNotNull(result);
            assertEquals("Nice event!", result.getComment());
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Test
    void createPrivateComment_eventNotPublished_throwsException() {
        event.setState(State.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(CommentValidationException.class,
                () -> commentService.createPrivateComment(1L, 1L, request));
    }

    @Test
    void updatePrivateComment_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        try (MockedStatic<CommentMapper> mockedMapper = mockStatic(CommentMapper.class)) {
            mockedMapper.when(() -> CommentMapper.mapToCommentDto(any(Comment.class)))
                    .thenReturn(commentDto);

            CommentDto result = commentService.updatePrivateComment(1L, 1L, 1L, request);

            assertEquals("Nice event!", result.getComment());
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Test
    void deletePrivateComment_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deletePrivateComment(1L, 1L, 1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void getPublicComment_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findAllByEventId(1L)).thenReturn(List.of(comment));

        try (MockedStatic<CommentMapper> mockedMapper = mockStatic(CommentMapper.class)) {
            mockedMapper.when(() -> CommentMapper.mapToCommentDto(any(Comment.class)))
                    .thenReturn(commentDto);

            List<CommentDto> result = commentService.getPublicComment(1L);

            assertEquals(1, result.size());
            assertEquals("Nice event!", result.get(0).getComment());
        }
    }

    @Test
    void getPrivateComment_success() {
        when(commentRepository.findAllByParams(1L, 1L, null))
                .thenReturn(List.of(comment));

        try (MockedStatic<CommentMapper> mockedMapper = mockStatic(CommentMapper.class)) {
            mockedMapper.when(() -> CommentMapper.mapToCommentDto(any(Comment.class)))
                    .thenReturn(commentDto);

            List<CommentDto> result = commentService.getPrivateComment(1L, 1L, null);

            assertEquals(1, result.size());
            assertEquals(commentDto, result.get(0));
        }
    }

    @Test
    void getAdminComment_withoutDates_success() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAllByAdminParams(eq(1L), eq(1L), eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        try (MockedStatic<CommentMapper> mockedMapper = mockStatic(CommentMapper.class)) {
            mockedMapper.when(() -> CommentMapper.mapToCommentDto(any(Comment.class)))
                    .thenReturn(commentDto);

            List<CommentDto> result = commentService.getAdminComment(1L, 1L, 1L, null, null, 0, 10);

            assertEquals(1, result.size());
            assertEquals("Nice event!", result.get(0).getComment());
        }
    }

    @Test
    void deleteAdminComment_withCommentId_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteAdminComment(1L, null, 1L);

        verify(commentRepository).deleteByIdAndEventId(1L, 1L);
    }

    @Test
    void deleteAdminComment_withUserId_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        commentService.deleteAdminComment(1L, 1L, null);

        verify(commentRepository).deleteAllByEventIdAndUserId(1L, 1L);
    }

    @Test
    void deleteAdminComment_withoutUserAndCommentId_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        commentService.deleteAdminComment(1L, null, null);

        verify(commentRepository).deleteByEvent(1L);
    }
}
