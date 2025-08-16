import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.server.repository.EndpointHitRepository;
import ru.practicum.ewm.server.service.StatisticServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class StatisticServiceTest {
    @Mock
    private EndpointHitRepository endpointHitRepository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateHit() {
        EndpointHit hit = new EndpointHit(null, "app1", "/test", "127.0.0.1", LocalDateTime.now());

        statisticService.createHit(hit);

        verify(endpointHitRepository, times(1)).save(hit);
    }

    @Test
    void testGetStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/test");
        Boolean unique = false;

        ViewStats stats = new ViewStats("app1", "/test", 5L);
        when(endpointHitRepository.getAllViewStats(start, end, uris, unique))
                .thenReturn(List.of(stats));

        List<ViewStats> result = statisticService.getStats(start, end, uris, unique);

        assertEquals(1, result.size());
        assertEquals("/test", result.get(0).getUri());
        assertEquals(5L, result.get(0).getHits());
    }
}




