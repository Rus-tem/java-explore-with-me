import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.main.controller.admin.AdminCompilationController;
import ru.practicum.ewm.main.controller.pub.PublicCompilationController;
import ru.practicum.ewm.main.dto.compilation.CompilationDto;
import ru.practicum.ewm.main.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.main.dto.event.EventShortDto;
import ru.practicum.ewm.main.service.api.CompilationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class CompilationControllerTest {

    private MockMvc mockMvc;
    private CompilationService compilationService;
    private ObjectMapper objectMapper;

    private AdminCompilationController adminController;
    private PublicCompilationController publicController;

    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;
    private UpdateCompilationRequest updateRequest;

    @BeforeEach
    void setUp() {
        compilationService = Mockito.mock(CompilationService.class);
        objectMapper = new ObjectMapper();

        adminController = new AdminCompilationController(compilationService);
        publicController = new PublicCompilationController(compilationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController, publicController)
                .build();

        EventShortDto eventShortDto = new EventShortDto(1L, "Event 1", null, 0L, null, null, null, null, null);

        compilationDto = new CompilationDto(1L, "Compilation 1", false, List.of(eventShortDto));
        newCompilationDto = new NewCompilationDto("New Compilation", false, List.of(1L));
        updateRequest = new UpdateCompilationRequest("Updated Compilation", true, List.of(1L));
    }

    // Admin. Создание подборки
    @Test
    void createCompilation_success() throws Exception {
        when(compilationService.createCompilation(any(NewCompilationDto.class))).thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));

        verify(compilationService, times(1)).createCompilation(any(NewCompilationDto.class));
    }

    // Admin. Удаление подборки
    @Test
    void deleteCompilation_success() throws Exception {
        doNothing().when(compilationService).deleteCompilation(anyLong());

        mockMvc.perform(delete("/admin/compilations/{compId}", 1L))
                .andExpect(status().isNoContent());

        verify(compilationService, times(1)).deleteCompilation(anyLong());
    }

    // Admin. Обновление подборки
    @Test
    void updateCompilation_success() throws Exception {
        when(compilationService.updateCompilation(anyLong(), any(UpdateCompilationRequest.class))).thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));

        verify(compilationService, times(1)).updateCompilation(anyLong(), any(UpdateCompilationRequest.class));
    }

    // Public. Получение всех подборок
    @Test
    void getCompilations_success() throws Exception {
        when(compilationService.getCompilations(false, 0, 10)).thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "false")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(compilationDto.getId()))
                .andExpect(jsonPath("$[0].title").value(compilationDto.getTitle()));

        verify(compilationService, times(1)).getCompilations(false, 0, 10);
    }

    // Public. Получение подборки по id
    @Test
    void getCompilationById_success() throws Exception {
        when(compilationService.getCompilationById(1L)).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/{compId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));

        verify(compilationService, times(1)).getCompilationById(1L);
    }
}