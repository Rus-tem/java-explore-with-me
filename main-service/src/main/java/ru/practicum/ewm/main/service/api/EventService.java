package ru.practicum.ewm.main.service.api;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.main.dto.event.*;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, int from, int size);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size);

    EventFullDto getPublicEventById(Long eventId,  HttpServletRequest request);

    List<EventShortDto> getPrivateUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createPrivateEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getPrivateUserEvent(Long userId, Long eventId);

    EventFullDto updatePrivateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List <ParticipationRequestDto> getPrivateUserEventRequests (Long userId, Long eventId );

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);
}
