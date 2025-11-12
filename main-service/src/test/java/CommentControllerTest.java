import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.main.controller.admin.AdminCommentController;
import ru.practicum.ewm.main.controller.priv.PrivateCommentController;
import ru.practicum.ewm.main.controller.pub.PublicCommentController;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentRequest;
import ru.practicum.ewm.main.service.api.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private AdminCommentController adminCommentController;

    @InjectMocks
    private PrivateCommentController privateCommentController;

    @InjectMocks
    private PublicCommentController publicCommentController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CommentDto commentDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(
                adminCommentController,
                privateCommentController,
                publicCommentController
        ).build();

        commentDto = new CommentDto(
                1L,
                "Test comment",
                null,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    void getAdminComment_shouldReturnList() throws Exception {
        when(commentService.getAdminComment(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/admin/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].comment").value(commentDto.getComment()));

        verify(commentService, times(1))
                .getAdminComment(any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void deleteAdminComment_shouldCallService() throws Exception {
        doNothing().when(commentService).deleteAdminComment(anyLong(), any(), any());

        mockMvc.perform(delete("/admin/comments/1")
                        .param("userId", "2")
                        .param("commentId", "3"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteAdminComment(1L, 2L, 3L);
    }

    @Test
    void createPrivateComment_shouldReturnCreated() throws Exception {
        CommentRequest request = new CommentRequest("New private comment");
        when(commentService.createPrivateComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/comments/1/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.comment").value(commentDto.getComment()));

        verify(commentService, times(1))
                .createPrivateComment(1L, 2L, request);
    }

    @Test
    void updatePrivateComment_shouldReturnUpdated() throws Exception {
        CommentRequest request = new CommentRequest("Updated text");
        when(commentService.updatePrivateComment(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mockMvc.perform(patch("/comments/1/2/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.comment").value(commentDto.getComment()));

        verify(commentService, times(1))
                .updatePrivateComment(1L, 2L, 3L, request);
    }

    @Test
    void deletePrivateComment_shouldCallService() throws Exception {
        doNothing().when(commentService).deletePrivateComment(anyLong(), anyLong(), any());

        mockMvc.perform(delete("/comments/1/2")
                        .param("commentId", "5"))
                .andExpect(status().isNoContent());

        verify(commentService, times(1))
                .deletePrivateComment(1L, 2L, 5L);
    }

    @Test
    void getPrivateComment_shouldReturnList() throws Exception {
        when(commentService.getPrivateComment(any(), any(), any()))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/comments")
                        .param("userId", "1")
                        .param("eventId", "2")
                        .param("commentId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].comment").value(commentDto.getComment()));

        verify(commentService, times(1))
                .getPrivateComment(1L, 2L, 3L);
    }

    @Test
    void getPublicComment_shouldReturnList() throws Exception {
        when(commentService.getPublicComment(anyLong()))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/public/comments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].comment").value(commentDto.getComment()));

        verify(commentService, times(1)).getPublicComment(10L);
    }
}
