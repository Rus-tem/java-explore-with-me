import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.main.controller.admin.AdminUserController;
import ru.practicum.ewm.main.controller.priv.PrivateUserRequestController;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.dto.user.NewUserRequest;
import ru.practicum.ewm.main.dto.user.UserDto;
import ru.practicum.ewm.main.service.api.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class UserControllerTest {

    private MockMvc mockMvcAdmin;
    private MockMvc mockMvcPrivate;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @InjectMocks
    private PrivateUserRequestController privateUserRequestController;

    private UserDto userDto;
    private NewUserRequest newUserRequest;
    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvcAdmin = MockMvcBuilders.standaloneSetup(adminUserController).build();
        mockMvcPrivate = MockMvcBuilders.standaloneSetup(privateUserRequestController).build();

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        newUserRequest = new NewUserRequest();
        newUserRequest.setName("John Doe");
        newUserRequest.setEmail("john@example.com");

        requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(1L);
        requestDto.setRequester(1L);
        requestDto.setStatus("PENDING");
    }

    @Test
    void createUser_success() throws Exception {
        when(userService.createAdminUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvcAdmin.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).createAdminUser(any(NewUserRequest.class));
    }

    @Test
    void getUsers_success() throws Exception {
        when(userService.getAdminUsers(nullable(List.class), anyInt(), anyInt()))
                .thenReturn(List.of(userDto));

        mockMvcAdmin.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));

        verify(userService, times(1)).getAdminUsers(nullable(List.class), anyInt(), anyInt());
    }

    @Test
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteAdminUser(anyLong());

        mockMvcAdmin.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteAdminUser(1L);
    }

    @Test
    void getPrivateUserRequests_success() throws Exception {
        when(userService.getPrivateUserRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvcPrivate.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()))
                .andExpect(jsonPath("$[0].event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(requestDto.getRequester()))
                .andExpect(jsonPath("$[0].status").value(requestDto.getStatus()));

        verify(userService, times(1)).getPrivateUserRequests(1L);
    }

    @Test
    void addPrivateRequest_success() throws Exception {
        when(userService.addPrivateRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvcPrivate.perform(post("/users/1/requests")
                        .param("eventId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(requestDto.getRequester()))
                .andExpect(jsonPath("$.status").value(requestDto.getStatus()));

        verify(userService, times(1)).addPrivateRequest(1L, 1L);
    }

    @Test
    void cancelPrivateRequest_success() throws Exception {
        requestDto.setStatus("CANCELED");
        when(userService.cancelPrivateRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvcPrivate.perform(patch("/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(userService, times(1)).cancelPrivateRequest(1L, 1L);
    }
}
