import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.ewm.main.controller.admin.AdminCategoryController;
import ru.practicum.ewm.main.controller.pub.PublicCategoryController;
import ru.practicum.ewm.main.dto.category.CategoryDto;
import ru.practicum.ewm.main.dto.category.NewCategoryDto;
import ru.practicum.ewm.main.service.api.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
class CategoryControllerTest {

    private MockMvc mockMvc;
    private CategoryService categoryService;
    private ObjectMapper objectMapper;

    private AdminCategoryController adminController;
    private PublicCategoryController publicController;

    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        categoryService = Mockito.mock(CategoryService.class);
        objectMapper = new ObjectMapper();

        adminController = new AdminCategoryController(categoryService);
        publicController = new PublicCategoryController(categoryService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController, publicController)
                .build();

        categoryDto = new CategoryDto(1L, "Music");
        newCategoryDto = new NewCategoryDto("Sports");
    }

    @Test
    void addCategory_success() throws Exception {
        when(categoryService.createAdminCategory(any(NewCategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService, times(1)).createAdminCategory(any(NewCategoryDto.class));
    }

    @Test
    void updateCategory_success() throws Exception {
        when(categoryService.updateAdminCategory(anyLong(), any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService, times(1)).updateAdminCategory(anyLong(), any(CategoryDto.class));
    }

    @Test
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteAdminCategory(anyLong());

        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteAdminCategory(anyLong());
    }

    @Test
    void getCategories_success() throws Exception {
        when(categoryService.getPublicCategories(0, 10)).thenReturn(List.of(categoryDto));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryDto.getId()))
                .andExpect(jsonPath("$[0].name").value(categoryDto.getName()));

        verify(categoryService, times(1)).getPublicCategories(0, 10);
    }

    @Test
    void getCategoryById_success() throws Exception {
        when(categoryService.getPublicCategoryById(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/{catId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService, times(1)).getPublicCategoryById(1L);
    }
}