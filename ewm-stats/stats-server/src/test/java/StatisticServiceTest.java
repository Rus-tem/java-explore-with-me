import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.server.repository.EndpointHitRepository;
import ru.practicum.ewm.server.service.StatisticServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class StatisticServiceTest {

    @Mock
    private EndpointHitRepository endpointHitRepository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateHit_callsRepositorySave() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("testApp");
        hit.setUri("/test");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(LocalDateTime.now());

        statisticService.createHit(hit);

        verify(endpointHitRepository, times(1)).save(hit);
    }

    @Test
    void testGetStats_returnsExpectedData() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<String> uris = Arrays.asList("/default");
        Boolean unique = false;

        List<ViewStats> expectedStats = Collections.singletonList(new ViewStats("testApp", "/default", 10L));

        when(endpointHitRepository.getAllViewStats(start, end, uris, unique))
                .thenReturn(expectedStats);

        List<ViewStats> actualStats = statisticService.getStats(start, end, uris, unique);

        assertEquals(expectedStats, actualStats);
        verify(endpointHitRepository, times(1)).getAllViewStats(start, end, uris, unique);
    }

    @Test
    void testGetStats_withNullUrisAndDefaultUnique() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<ViewStats> expectedStats = Collections.singletonList(new ViewStats("testApp", "/default", 10L));

        when(endpointHitRepository.getAllViewStats(start, end, null, false))
                .thenReturn(expectedStats);

        List<ViewStats> actualStats = statisticService.getStats(start, end, null, false);

        assertEquals(expectedStats, actualStats);
        verify(endpointHitRepository, times(1)).getAllViewStats(start, end, null, false);
    }
}




