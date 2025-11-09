package ru.practicum.ewm.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.server.exceptions.BadTimeRequest;
import ru.practicum.ewm.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final EndpointHitRepository endpointHitRepository;

    @Override
    public void createHit(EndpointHit endpointHit) {
        endpointHitRepository.save(endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start.isAfter(end)) {
            throw new BadTimeRequest("Время начала поиска должно быть раньше времени конца поиска");
        }
        return endpointHitRepository.getAllViewStats(start, end, uris, unique);
    }
}
