import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.server.controller.StatisticController;
import ru.practicum.ewm.server.service.StatisticService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class StatisticControllerTest {

    @Mock
    private StatisticService statisticService;

    @InjectMocks
    private StatisticController statisticController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateHits_callsServiceWithCorrectArgument() {
        EndpointHit hit = new EndpointHit(null, "testApp", "/test", "127.0.0.1", LocalDateTime.now());

        statisticController.createHits(hit);

        verify(statisticService, times(1)).createHit(hit);
    }

    @Test
    void testGetStats_returnsCorrectStats() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 17, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 8, 17, 23, 59);

        List<String> uris = List.of("/test");
        boolean unique = true;

        List<ViewStats> expectedStats = List.of(
                new ViewStats("testApp", "/test", 5L)
        );

        when(statisticService.getStats(start, end, uris, unique)).thenReturn(expectedStats);

        List<ViewStats> actualStats = statisticController.getStats(start, end, uris, unique);

        assertEquals(expectedStats, actualStats);
        verify(statisticService, times(1)).getStats(start, end, uris, unique);
    }

    @Test
    void testGetStats_withNullUrisAndDefaultUnique() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 17, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 8, 17, 23, 59);

        List<ViewStats> expectedStats = List.of(
                new ViewStats("testApp", "/default", 10L)
        );

        when(statisticService.getStats(start, end, null, false)).thenReturn(expectedStats);

        List<ViewStats> actualStats = statisticController.getStats(start, end, null, false);

        assertEquals(expectedStats, actualStats);
        verify(statisticService, times(1)).getStats(start, end, null, false);
    }
}