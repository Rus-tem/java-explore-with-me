import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.main.dto.compilation.CompilationDto;
import ru.practicum.ewm.main.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.main.exception.CompilationNotFoundException;
import ru.practicum.ewm.main.exception.CompilationValidationException;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.impl.CompilationServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class CompilationServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationRepository compilationRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCompilation_whenTitleNull_shouldThrowException() {
        NewCompilationDto dto = new NewCompilationDto(null, true, null);

        CompilationValidationException ex = assertThrows(
                CompilationValidationException.class,
                () -> compilationService.createCompilation(dto)
        );

        assertEquals("Не указан title compilation", ex.getMessage());
        verifyNoInteractions(eventRepository, compilationRepository);
    }

    @Test
    void createCompilation_whenPinnedNull_shouldThrowException() {
        NewCompilationDto dto = new NewCompilationDto("test", null, null);

        CompilationValidationException ex = assertThrows(
                CompilationValidationException.class,
                () -> compilationService.createCompilation(dto)
        );

        assertEquals("Не указано поле pinned compilation", ex.getMessage());
        verifyNoInteractions(eventRepository, compilationRepository);
    }

    @Test
    void createCompilation_shouldSaveCompilation() {
        NewCompilationDto dto = new NewCompilationDto("test", true, List.of(1L, 2L));

        when(eventRepository.findAllById(dto.getEvents()))
                .thenReturn(List.of(makeEvent(1L, 11L, 111L), makeEvent(2L, 22L, 222L)));

        Compilation saved = Compilation.builder()
                .id(10L)
                .title("test")
                .pinned(true)
                .events(new HashSet<>(List.of(makeEvent(1L, 11L, 111L), makeEvent(2L, 22L, 222L))))
                .build();

        when(compilationRepository.save(any()))
                .thenReturn(saved);

        CompilationDto result = compilationService.createCompilation(dto);

        assertEquals("test", result.getTitle());
        assertEquals(true, result.getPinned());
        assertEquals(10L, result.getId());
        assertEquals(2, result.getEvents().size());
    }

    @Test
    void updateCompilation_shouldUpdateFields() {
        Compilation existing = Compilation.builder()
                .id(1L)
                .title("old")
                .pinned(false)
                .events(new HashSet<>())
                .build();

        UpdateCompilationRequest update = new UpdateCompilationRequest("new", true, List.of(7L));

        when(compilationRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(eventRepository.findAllById(update.getEvents()))
                .thenReturn(List.of(makeEvent(7L, 77L, 777L)));

        Compilation updated = Compilation.builder()
                .id(1L)
                .title("new")
                .pinned(true)
                .events(Set.of(makeEvent(7L, 77L, 777L)))
                .build();

        when(compilationRepository.save(existing))
                .thenReturn(updated);

        CompilationDto result = compilationService.updateCompilation(1L, update);

        assertEquals("new", result.getTitle());
        assertEquals(true, result.getPinned());
        assertEquals(1, result.getEvents().size());
    }


    @Test
    void deleteCompilation_whenNotFound_shouldThrow() {
        when(compilationRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.deleteCompilation(5L)
        );
    }

    @Test
    void deleteCompilation_shouldDelete() {
        Compilation c = new Compilation();
        when(compilationRepository.findById(1L))
                .thenReturn(Optional.of(c));

        compilationService.deleteCompilation(1L);

        verify(compilationRepository).delete(c);
    }

    @Test
    void getCompilations_shouldReturnList() {
        Compilation c = Compilation.builder()
                .id(1L).title("t1").pinned(true)
                .events(new HashSet<>())
                .build();

        Page<Compilation> page = new PageImpl<>(List.of(c));

        when(compilationRepository.findAll(any(PageRequest.class)))
                .thenReturn(page);

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTitle());
    }

    @Test
    void getCompilationById_whenNotFound_shouldThrow() {
        when(compilationRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.getCompilationById(2L)
        );
    }

    @Test
    void getCompilationById_shouldReturnDto() {
        Compilation cmp = Compilation.builder()
                .id(1L).title("x").pinned(false)
                .events(new HashSet<>())
                .build();

        when(compilationRepository.findById(1L))
                .thenReturn(Optional.of(cmp));

        CompilationDto result = compilationService.getCompilationById(1L);

        assertEquals("x", result.getTitle());
        assertEquals(1L, result.getId());
    }

    private Event makeEvent(Long eventId, Long categoryId, Long userId) {

        Event event = new Event();
        event.setId(eventId);

        ru.practicum.ewm.main.model.Category category = new ru.practicum.ewm.main.model.Category();
        category.setId(categoryId);
        event.setCategory(category);

        ru.practicum.ewm.main.model.User user = new ru.practicum.ewm.main.model.User();
        user.setId(userId);
        event.setInitiator(user);

        return event;
    }
}
