package ru.practicum.ewm.main.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.main.service.api.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateUserRequestController {

    private final UserService userService;

    // Private. Получение информации о заявках текущего пользователя на участие в событиях
    @GetMapping
    public List<ParticipationRequestDto> getPrivateUserRequests(@PathVariable Long userId) {
        return userService.getPrivateUserRequests(userId);
    }

    // Private. Добавление запроса на участие в событии
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addPrivateRequest(@PathVariable Long userId,
                                                     @RequestParam Long eventId) {
        return userService.addPrivateRequest(userId, eventId);
    }

    // Private. Отмена своего запроса на участие в событии
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelPrivateRequest(@PathVariable Long userId,
                                                        @PathVariable Long requestId) {
        return userService.cancelPrivateRequest(userId, requestId);
    }

}
