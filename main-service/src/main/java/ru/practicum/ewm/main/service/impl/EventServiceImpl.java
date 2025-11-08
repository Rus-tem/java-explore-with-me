package ru.practicum.ewm.main.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatisticClient;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.dto.event.*;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.exception.*;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.mapper.LocationMapper;
import ru.practicum.ewm.main.mapper.RequestMapper;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.ParticipationRequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.api.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatisticClient statisticClient;

    //Admin. Поиск Event
    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             String rangeStart,
                                             String rangeEnd,
                                             int from,
                                             int size) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime rangeStartFormatted;
        LocalDateTime rangeEndFormatted;

        if (rangeStart == null || rangeEnd.isBlank()) {
            rangeStartFormatted = null;
        } else {
            rangeStartFormatted = LocalDateTime.parse(rangeStart, formatter);
        }
        if (rangeEnd == null || rangeEnd.isBlank()) {
            rangeEndFormatted = null;
        } else {
            rangeEndFormatted = LocalDateTime.parse(rangeEnd, formatter);
        }
        Pageable pageRequest = PageRequest.of(from / size, size);
        List<State> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            stateEnums = states.stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());
        }
        Page<Event> events = eventRepository.findEventsByAdminFilters(
                users, stateEnums, categories, rangeStartFormatted, rangeEndFormatted, pageRequest
        );

        return events.stream()
                .map(EventMapper::mapToEventFullDto)
                .collect(Collectors.toList());
    }

    // Admin. Обновление Event
    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event с таким номером = " + eventId + " не найден"));
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                throw new EventConflictException("Event" + eventId + " уже опубликован и не может быть отменен");
            } else if (request.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && event.getState().equals(State.PUBLISHED)) {
                throw new EventConflictException("Event " + eventId + " уже опубликован");
            } else if (request.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && event.getState().equals(State.PENDING)) {
                event.setState(State.PUBLISHED);
            } else if (request.getStateAction().equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(State.PENDING)) {
                event.setState(State.CANCELED);
                eventRepository.save(event);
                return EventMapper.mapToEventFullDto(event);
            } else if (request.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && event.getState().equals(State.CANCELED)) {
                throw new EventConflictException("Event " + eventId + " был отменен и не может быть опубликован");
            }
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new UserConflictException("Превышен лимит участников Event");
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            LocalDateTime newDate = request.getEventDate();
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new RequestValidationException("Дата Event должна быть не менее чем на 1 час позже от текущего времени.");
            }
            event.setEventDate(newDate);
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Category с таким номером = " + request.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (request.getLocation() != null) {
            event.setLocation(LocationMapper.mapToLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        Event updated = eventRepository.save(event);

        return EventMapper.mapToEventFullDto(updated);
    }

    // Public. Получение списка Event
    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               Integer from,
                                               Integer size,
                                               HttpServletRequest request) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime rangeStartFormatted;
        LocalDateTime rangeEndFormatted;

        if (rangeStart == null || rangeEnd.isBlank()) {
            Event event = eventRepository.findFirstByOrderByCreatedOnAsc().orElseThrow(() ->
                    new EventNotFoundException("Event не найден"));
            rangeStartFormatted = event.getCreatedOn();
        } else {
            rangeStartFormatted = LocalDateTime.parse(rangeStart, formatter);
        }
        if (rangeEnd == null || rangeEnd.isBlank()) {
            Event event = eventRepository.findFirstByOrderByCreatedOnDesc().orElseThrow(() ->
                    new EventNotFoundException("Event не найден"));
            rangeEndFormatted = event.getCreatedOn();
        } else {
            rangeEndFormatted = LocalDateTime.parse(rangeEnd, formatter);
        }
        if (rangeStartFormatted != null || rangeEndFormatted != null) {
            if (rangeEndFormatted.isBefore(rangeStartFormatted)) {
                throw new EventValidationException("Время окончание события не должно быть раньше времени начала");
            }
        }
        Sort sortObj;
        if ("VIEWS".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Order.desc("views"));
        } else {
            sortObj = Sort.by(Sort.Order.asc("eventDate"));
        }
        Pageable pageRequest = PageRequest.of(from / size, size, sortObj);
        Page<Event> events;

        if (text == null) {
            events = eventRepository.searchPublicEvents(
                    categories,
                    paid,
                    onlyAvailable,
                    pageRequest
            );
        } else {
            events = eventRepository.searchPublicEventsAllParam(
                    text,
                    categories,
                    paid,
                    rangeStartFormatted,
                    rangeEndFormatted,
                    onlyAvailable,
                    pageRequest
            );
        }
        statisticClient.endpointHit(request);
        return events.stream()
                .map(EventMapper::eventMapToEventShortDto)
                .toList();
    }

    // Public. Получение Event по Id
    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event не найден: " + eventId));

        if (event.getState() != State.PUBLISHED) {
            throw new EventNotFoundException("Event не опубликован: " + eventId);
        }
        if (event.getViews() < 1) {
            event.setViews(event.getViews() + 1);
        }
        eventRepository.save(event);
        statisticClient.endpointHit(request);

        return EventMapper.mapToEventFullDto(event);
    }

    // Private. Получение списка Event по параметрам
    @Override
    public List<EventShortDto> getPrivateUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::eventMapToEventShortDto)
                .toList();
    }

    // Private. Добавление нового события
    @Override
    public EventFullDto createPrivateEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Category не найдена"));
        checkEvent(newEventDto);
        Event event = EventMapper.mapNewEventDtoToEvent(newEventDto, category, initiator);

        event.setConfirmedRequests(0L);
        event.setPublishedOn(LocalDateTime.now());
        event.setViews(0L);
        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        if (event.getEventDate().isBefore(event.getPublishedOn())) {
            throw new EventValidationException("Дата Event не может быть раньше даты публикации " + event.getPublishedOn());
        }
        Event saved = eventRepository.save(event);

        return EventMapper.mapToEventFullDto(saved);
    }

    // Private. Получение Event
    @Override
    public EventFullDto getPrivateUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException("Event не найден или не принадлежит User"));
        return EventMapper.mapToEventFullDto(event);
    }

    // Private. Обновление Event текущим пользователем
    @Override
    public EventFullDto updatePrivateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventConflictException("Event не найден или не принадлежит User"));

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            } else if (updateRequest.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
                eventRepository.save(event);
                return EventMapper.mapToEventFullDto(event);
            }
        }

        if (!(event.getState() == State.PENDING || event.getState() == State.CANCELED)) {
            throw new EventConflictException("Редактировать можно только Event в статусе PENDING или CANCELED");
        }

        if (updateRequest.getParticipantLimit() != null) {
            if (updateRequest.getParticipantLimit() < 0) {
                throw new EventValidationException("Отрицательное значение участников Event");
            }
        }
        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(event.getPublishedOn())) {
                throw new EventValidationException("Дата Event не может быть раньше чем дата публикации " + event.getPublishedOn());
            }
        }
        EventMapper.updateEventFromUserRequest(updateRequest, event);
        Category category = categoryRepository.findById(event.getCategory().getId()).orElseThrow(() ->
                new CategoryNotFoundException("Category с таким Id = " + updateRequest.getCategory() + " не найдена"));
        event.setCategory(category);
        Event updated = eventRepository.save(event);

        return EventMapper.mapToEventFullDto(updated);
    }

    // Private. Получение информации о запросах на участие в событии текущего пользователя
    @Override
    public List<ParticipationRequestDto> getPrivateUserEventRequests(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventValidationException("User не является инициатором события");
        }

        // Находим все заявки по событию
        List<ParticipationRequest> requests = participationRequestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    //Private. Изменение статуса(подтверждения, отмены) заявок на участие в событии текущего пользователя
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventValidationException("User не является инициатором события");
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new UserConflictException("Превышен лимит участников Event");
        }
        List<ParticipationRequest> requests = participationRequestRepository.findAllById(updateRequest.getRequestIds());
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals(RequestStatus.CANCELED)) {
                throw new EventConflictException("Можно изменять только заявки в статусе PENDING или CONFIRMED");
            }
            boolean eventLimit = event.getParticipantLimit() != 0 &&
                                 event.getConfirmedRequests() >= event.getParticipantLimit();

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED && !eventLimit) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }
        participationRequestRepository.saveAll(requests);
        eventRepository.save(event);

        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream().map(RequestMapper::mapToParticipationRequestDto).toList(),
                rejectedRequests.stream().map(RequestMapper::mapToParticipationRequestDto).toList()
        );
    }

    // Метод проверки полей Event
    private void checkEvent(NewEventDto newEventDto) {
        if (newEventDto.getDescription() == null || newEventDto.getDescription().isBlank()) {
            throw new EventValidationException("Не корректно заполнено поле description");
        }
        if (newEventDto.getAnnotation() == null || newEventDto.getAnnotation().isBlank()) {
            throw new EventValidationException("Не корректно заполнено поле annotation");
        }
        if (newEventDto.getParticipantLimit() == null || newEventDto.getParticipantLimit() < 0) {
            throw new EventValidationException("Не корректно заполнено поле annotation");
        }
    }
}
