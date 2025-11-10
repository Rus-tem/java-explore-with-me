import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.main.controller.admin.AdminEventController;
import ru.practicum.ewm.main.controller.priv.PrivateEventController;
import ru.practicum.ewm.main.controller.pub.PublicEventController;
import ru.practicum.ewm.main.dto.event.*;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.service.api.EventService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class EventControllerTest {
    @Mock
    private EventService eventService;

    @InjectMocks
    private PrivateEventController privateController;

    @InjectMocks
    private PublicEventController publicController;

    @InjectMocks
    private AdminEventController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPrivateUserEvents_success() {
        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        when(eventService.getPrivateUserEvents(1L, 0, 10)).thenReturn(List.of(dto));

        var result = privateController.getPrivateUserEvents(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(eventService).getPrivateUserEvents(1L, 0, 10);
    }

    @Test
    void getPrivateUserEvent_success() {
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);
        when(eventService.getPrivateUserEvent(1L, 1L)).thenReturn(dto);

        var result = privateController.getPrivateUserEvent(1L, 1L);

        assertEquals(1L, result.getId());
        verify(eventService).getPrivateUserEvent(1L, 1L);
    }

    @Test
    void getPrivateUserEventRequests_success() {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);

        when(eventService.getPrivateUserEventRequests(1L, 1L))
                .thenReturn(List.of(requestDto));

        var result = privateController.getPrivateUserEventRequests(1L, 1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(eventService).getPrivateUserEventRequests(1L, 1L);
    }

    @Test
    void createPrivateEvent_success() {
        NewEventDto newEvent = new NewEventDto();
        newEvent.setTitle("Test Event");
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);

        when(eventService.createPrivateEvent(1L, newEvent)).thenReturn(dto);

        var result = privateController.createPrivateEvent(1L, newEvent);

        assertEquals(1L, result.getId());
        verify(eventService).createPrivateEvent(1L, newEvent);
    }

    @Test
    void updatePrivateUserEvent_success() {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);

        when(eventService.updatePrivateUserEvent(1L, 1L, updateRequest)).thenReturn(dto);

        var result = privateController.updatePrivateUserEvent(1L, 1L, updateRequest);

        assertEquals(1L, result.getId());
        verify(eventService).updatePrivateUserEvent(1L, 1L, updateRequest);
    }

    @Test
    void updateRequestStatus_success() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);

        EventRequestStatusUpdateResult resultDto = new EventRequestStatusUpdateResult(List.of(requestDto), List.of());

        when(eventService.updateRequestStatus(1L, 1L, updateRequest)).thenReturn(resultDto);

        var result = privateController.updateRequestStatus(1L, 1L, updateRequest);

        assertEquals(1, result.getConfirmedRequests().size());
        assertEquals(1L, result.getConfirmedRequests().get(0).getId());
        verify(eventService).updateRequestStatus(1L, 1L, updateRequest);
    }

    @Test
    void getPublicEvents_success() {
        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(eventService.getPublicEvents(null, null, null, null, null, false,
                "EVENT_DATE", 0, 10, request)).thenReturn(List.of(dto));

        var result = publicController.getPublicEvents(null, null, null, null, null,
                false, "EVENT_DATE", 0, 10, request);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(eventService).getPublicEvents(null, null, null, null, null,
                false, "EVENT_DATE", 0, 10, request);
    }

    @Test
    void getPublicEventById_success() {
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(eventService.getPublicEventById(1L, request)).thenReturn(dto);

        var result = publicController.getPublicEventById(1L, request);

        assertEquals(1L, result.getId());
        verify(eventService).getPublicEventById(1L, request);
    }

    @Test
    void getAdminEvents_success() {
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);

        when(eventService.getAdminEvents(null, null, null, null, null, 0, 10))
                .thenReturn(List.of(dto));

        var result = adminController.getAdminEvents(null, null, null, null, null, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(eventService).getAdminEvents(null, null, null, null, null, 0, 10);
    }

    @Test
    void updateAdminEvent_success() {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);

        when(eventService.updateAdminEvent(1L, request)).thenReturn(dto);

        var result = adminController.updateAdminEvent(1L, request);

        assertEquals(1L, result.getId());
        verify(eventService).updateAdminEvent(1L, request);
    }

}
