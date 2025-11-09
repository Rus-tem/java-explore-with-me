import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.dto.user.NewUserRequest;
import ru.practicum.ewm.main.dto.user.UserDto;
import ru.practicum.ewm.main.exception.UserConflictException;
import ru.practicum.ewm.main.exception.UserNotFoundException;
import ru.practicum.ewm.main.exception.UserValidationException;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.ParticipationRequest;
import ru.practicum.ewm.main.model.RequestStatus;
import ru.practicum.ewm.main.model.User;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.impl.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAdminUser_success() {
        NewUserRequest request = new NewUserRequest("John", "john@example.com");
        User user = new User(1L, "John", "john@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createAdminUser(request);

        assertEquals("John", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdminUser_duplicateEmail_shouldThrow() {
        NewUserRequest request = new NewUserRequest("John", "john@example.com");
        User existing = new User(1L, "Existing", "john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        assertThrows(UserConflictException.class, () -> userService.createAdminUser(request));
    }

    @Test
    void createAdminUser_invalidName_shouldThrow() {
        NewUserRequest request = new NewUserRequest(" ", "john@example.com");

        assertThrows(UserValidationException.class, () -> userService.createAdminUser(request));
    }

    @Test
    void getAdminUsers_withIds_success() {
        User u1 = new User(1L, "John", "j1@example.com");
        User u2 = new User(2L, "Alice", "a@example.com");
        Page<User> page = new PageImpl<>(List.of(u1, u2));

        when(userRepository.findAllByIdIn(anyList(), any(Pageable.class))).thenReturn(page);

        List<UserDto> result = userService.getAdminUsers(List.of(1L, 2L), 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getAdminUsers_noIds_success() {
        User u1 = new User(1L, "John", "j1@example.com");
        Page<User> page = new PageImpl<>(List.of(u1));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<UserDto> result = userService.getAdminUsers(null, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void deleteAdminUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteAdminUser(1L));

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteAdminUser_notFound_shouldThrow() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteAdminUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getPrivateUserRequests_success() {
        User user = new User(1L, "John", "j@example.com");
        Event event = new Event();
        ParticipationRequest req = new ParticipationRequest(1L, event, user, LocalDateTime.now(), RequestStatus.PENDING);

        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(req));

        List<ParticipationRequestDto> result = userService.getPrivateUserRequests(1L);

        assertEquals(1, result.size());
    }

    @Test
    void addPrivateRequest_success_confirmed() {
        User user = new User(1L, "John", "j@example.com");
        User initiator = new User(2L, "Bob", "b@example.com");
        Event event = new Event();
        event.setId(1L);
        event.setInitiator(initiator);
        event.setState(State.PUBLISHED);
        event.setParticipantLimit(0L);
        event.setConfirmedRequests(0L);
        event.setRequestModeration(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(1L, 1L)).thenReturn(false);
        when(requestRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ParticipationRequestDto result = userService.addPrivateRequest(1L, 1L);

        assertEquals(RequestStatus.CONFIRMED, RequestStatus.valueOf(result.getStatus()));
    }

    @Test
    void cancelPrivateRequest_success() {
        User user = new User(1L, "John", "j@example.com");
        Event event = new Event();
        ParticipationRequest req = new ParticipationRequest(1L, event, user, LocalDateTime.now(), RequestStatus.PENDING);

        when(requestRepository.findByIdAndRequesterId(1L, 1L)).thenReturn(Optional.of(req));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ParticipationRequestDto result = userService.cancelPrivateRequest(1L, 1L);

        assertEquals(RequestStatus.CANCELED, RequestStatus.valueOf(result.getStatus()));
    }


}
