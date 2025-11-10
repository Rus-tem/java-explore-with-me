package ru.practicum.ewm.main.service.api;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.main.dto.event.*;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    //Admin. Поиск Event
    List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, int from, int size);

    // Admin. Обновление Event
    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request);

    // Public. Получение списка Event
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size, HttpServletRequest request);

    // Public. Получение Event по Id
    EventFullDto getPublicEventById(Long eventId, HttpServletRequest request);

    // Private. Получение списка Event по параметрам
    List<EventShortDto> getPrivateUserEvents(Long userId, Integer from, Integer size);

    // Private. Добавление нового события
    EventFullDto createPrivateEvent(Long userId, NewEventDto newEventDto);

    // Private. Получение Event
    EventFullDto getPrivateUserEvent(Long userId, Long eventId);

    // Private. Обновление Event текущим пользователем
    EventFullDto updatePrivateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    // Private. Получение информации о запросах на участие в событии текущего пользователя
    List<ParticipationRequestDto> getPrivateUserEventRequests(Long userId, Long eventId);

    //Private. Изменение статуса(подтверждения, отмены) заявок на участие в событии текущего пользователя
    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);
}
