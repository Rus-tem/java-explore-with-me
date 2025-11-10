package ru.practicum.ewm.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadTimeRequest extends RuntimeException {
    public BadTimeRequest(String message) {
        super(message);
    }
}
