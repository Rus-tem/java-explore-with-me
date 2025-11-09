import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.client.StatisticClient;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.dto.event.*;
import ru.practicum.ewm.main.exception.*;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.ParticipationRequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.impl.EventServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class EventServiceTest {

    @InjectMocks
    private EventServiceImpl eventService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ParticipationRequestRepository participationRequestRepository;
    @Mock
    private StatisticClient statisticClient;

    private User user;
    private Category category;
    private Event event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User(1L, "Test User", "user@test.com");
        category = new Category(1L, "Music");
        event = new Event();
        event.setId(1L);
        event.setAnnotation("This is a test annotation for event");
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription("This is a test description for event with enough length");
        event.setEventDate(LocalDateTime.now().plusDays(2));
        event.setInitiator(user);
        event.setLocation(new Location(1L, 12, 34));
        event.setPaid(true);
        event.setParticipantLimit(10L);
        event.setPublishedOn(LocalDateTime.now());
        event.setRequestModeration(true);
        event.setState(State.PENDING);
        event.setTitle("Test Event Title");
        event.setViews(0L);
    }

    @Test
    void createPrivateEvent_success() {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setAnnotation("Это корректная аннотация события более 20 символов");
        newEventDto.setCategory(1L);
        newEventDto.setDescription("Это подробное описание события, больше 20 символов");
        newEventDto.setEventDate("2025-11-11 20:39:00"); // <-- корректный формат для маппера
        Location location = new Location();
        location.setLat(55);
        location.setLon(37);
        newEventDto.setLocation(location);
        newEventDto.setPaid(true);
        newEventDto.setParticipantLimit(100L);
        newEventDto.setRequestModeration(true);
        newEventDto.setTitle("Название события");

        User user = new User();
        user.setId(1L);
        Category category = new Category();
        category.setId(1L);

        Event savedEvent = EventMapper.mapNewEventDtoToEvent(newEventDto, category, user);
        savedEvent.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventFullDto result = eventService.createPrivateEvent(1L, newEventDto);

        assertNotNull(result);
        assertEquals(savedEvent.getId(), result.getId());
        assertEquals(newEventDto.getTitle(), result.getTitle());
        assertEquals(newEventDto.getAnnotation(), result.getAnnotation());
        assertEquals(newEventDto.getDescription(), result.getDescription());
        assertEquals(newEventDto.getEventDate(), result.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Test
    void createPrivateEvent_userNotFound_shouldThrow() {
        NewEventDto dto = new NewEventDto();
        dto.setCategory(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> eventService.createPrivateEvent(1L, dto));
    }

    @Test
    void createPrivateEvent_categoryNotFound_shouldThrow() {
        NewEventDto dto = new NewEventDto();
        dto.setCategory(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> eventService.createPrivateEvent(1L, dto));
    }

    @Test
    void getPrivateUserEvents_success() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        User initiator = new User();
        initiator.setId(1L);
        initiator.setName("Test User");
        initiator.setEmail("test@example.com");

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());

        List<Event> events = List.of(event);
        when(eventRepository.findAllByInitiatorId(
                eq(1L),
                any(Pageable.class))
        ).thenReturn(events);

        var result = eventService.getPrivateUserEvents(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getId());
        assertEquals(category.getId(), result.get(0).getCategory().getId());
        assertEquals(initiator.getId(), result.get(0).getInitiator().getId());
    }

    @Test
    void getPrivateUserEvent_success() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        var result = eventService.getPrivateUserEvent(1L, 1L);

        assertEquals(event.getId(), result.getId());
    }

    @Test
    void getPrivateUserEvent_notFound_shouldThrow() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventService.getPrivateUserEvent(1L, 1L));
    }

    @Test
    void updatePrivateUserEvent_success() {
        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setTitle("Updated Title");
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(event.getCategory().getId())).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        var result = eventService.updatePrivateUserEvent(1L, 1L, request);

        assertEquals("Updated Title", result.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updatePrivateUserEvent_notFound_shouldThrow() {
        UpdateEventUserRequest request = new UpdateEventUserRequest();
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(EventConflictException.class, () -> eventService.updatePrivateUserEvent(1L, 1L, request));
    }

    @Test
    void getPrivateUserEventRequests_success() {
        List<ParticipationRequest> requests = List.of(
                new ParticipationRequest(1L, event, user, LocalDateTime.now(), RequestStatus.PENDING)
        );
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(requests);

        var result = eventService.getPrivateUserEventRequests(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void getPrivateUserEventRequests_userNotInitiator_shouldThrow() {
        User anotherUser = new User(2L, "Other", "other@test.com");
        event.setInitiator(anotherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(EventValidationException.class, () -> eventService.getPrivateUserEventRequests(1L, 1L));
    }

    @Test
    void updateRequestStatus_successConfirmAndReject() {
        event.setParticipantLimit(2L);
        event.setConfirmedRequests(0L);
        ParticipationRequest r1 = new ParticipationRequest(1L, event, user, LocalDateTime.now(), RequestStatus.PENDING);
        ParticipationRequest r2 = new ParticipationRequest(2L, event, user, LocalDateTime.now(), RequestStatus.PENDING);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(r1, r2));
        when(participationRequestRepository.saveAll(anyList())).thenReturn(List.of(r1, r2));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        var request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L, 2L));
        request.setStatus(RequestStatus.CONFIRMED);

        var result = eventService.updateRequestStatus(1L, 1L, request);

        assertEquals(2, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
    }

    @Test
    void getPublicEventById_success() {
        event.setState(State.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        var result = eventService.getPublicEventById(1L, mock(HttpServletRequest.class));

        assertEquals(event.getId(), result.getId());
        assertEquals(0L, result.getViews());
    }

    @Test
    void getPublicEventById_notPublished_shouldThrow() {
        event.setState(State.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(EventNotFoundException.class, () -> eventService.getPublicEventById(1L, mock(HttpServletRequest.class)));
    }

    @Test
    void getAdminEvents_success() {
        Page<Event> page = new PageImpl<>(List.of(event));
        when(eventRepository.findEventsByAdminFilters(anyList(), anyList(), anyList(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        var result = eventService.getAdminEvents(List.of(1L), List.of("PENDING"), List.of(1L),
                null, null, 0, 10);

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getId());
    }

    @Test
    void updateAdminEvent_publishPending_success() {
        event.setState(State.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        var request = new UpdateEventAdminRequest();
        request.setStateAction(StateActionAdmin.PUBLISH_EVENT);

        var result = eventService.updateAdminEvent(1L, request);

        assertEquals(State.PUBLISHED, result.getState());
    }

    @Test
    void updateAdminEvent_rejectPending_success() {
        event.setState(State.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        var request = new UpdateEventAdminRequest();
        request.setStateAction(StateActionAdmin.REJECT_EVENT);

        var result = eventService.updateAdminEvent(1L, request);

        assertEquals(State.CANCELED, result.getState());
    }
}
