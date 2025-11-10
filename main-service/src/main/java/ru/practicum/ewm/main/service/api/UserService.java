package ru.practicum.ewm.main.service.api;

import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.dto.user.NewUserRequest;
import ru.practicum.ewm.main.dto.user.UserDto;

import java.util.List;

public interface UserService {
    // Admin. Создание пользователя
    UserDto createAdminUser(NewUserRequest newUserRequest);

    // Admin. Получение списка пользователя
    List<UserDto> getAdminUsers(List<Long> ids, int from, int size);

    // Admin. Удаление пользователя
    void deleteAdminUser(Long userId);

    // Private. Получение информации о заявках текущего пользователя на участие в событиях
    List<ParticipationRequestDto> getPrivateUserRequests(Long userId);

    // Private. Добавление запроса
    ParticipationRequestDto addPrivateRequest(Long userId, Long eventId);

    // Private. Отмена своего запроса на участие в событии
    ParticipationRequestDto cancelPrivateRequest(Long userId, Long requestId);
}
