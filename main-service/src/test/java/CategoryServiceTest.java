import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.main.dto.category.CategoryDto;
import ru.practicum.ewm.main.dto.category.NewCategoryDto;
import ru.practicum.ewm.main.exception.CategoryConflictException;
import ru.practicum.ewm.main.exception.CategoryNotFoundException;
import ru.practicum.ewm.main.exception.CategoryValidationException;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAdminCategory_success() {
        NewCategoryDto dto = new NewCategoryDto("Sport");
        Category category = new Category(1L, "Sport");

        when(categoryRepository.existsByName(dto.getName())).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDto result = categoryService.createAdminCategory(dto);

        assertNotNull(result);
        assertEquals("Sport", result.getName());
        verify(categoryRepository).save(any());
    }

    @Test
    void createAdminCategory_shouldThrowValidation_ifNameInvalid() {
        NewCategoryDto dto = new NewCategoryDto("   ");

        assertThrows(
                CategoryValidationException.class,
                () -> categoryService.createAdminCategory(dto)
        );
    }

    @Test
    void createAdminCategory_duplicateName_shouldThrowConflict() {
        NewCategoryDto dto = new NewCategoryDto("Sport");

        when(categoryRepository.existsByName("Sport")).thenReturn(true);

        assertThrows(
                CategoryConflictException.class,
                () -> categoryService.createAdminCategory(dto)
        );
    }

    @Test
    void updateAdminCategory_success() {
        long id = 1L;
        Category oldCategory = new Category(1L, "Old");
        CategoryDto dto = new CategoryDto(1L, "New");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(oldCategory));
        when(categoryRepository.existsByName(dto.getName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category(id, dto.getName()));

        CategoryDto result = categoryService.updateAdminCategory(id, dto);

        assertEquals("New", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateAdminCategory_notFound_shouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateAdminCategory(1L, new CategoryDto(1L, "New"))
        );
    }

    @Test
    void updateAdminCategory_duplicateNewName_shouldThrowConflict() {
        Category old = new Category(1L, "Old");
        CategoryDto dto = new CategoryDto(1L, "New");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(old));
        when(categoryRepository.existsByName("New")).thenReturn(true);

        assertThrows(
                CategoryConflictException.class,
                () -> categoryService.updateAdminCategory(1L, dto)
        );
    }

    @Test
    void deleteAdminCategory_success() {
        Category category = new Category(1L, "Sport");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteAdminCategory(1L));

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteAdminCategory_notFound_shouldThrow() {
        // Важно: мокируем точно тот же аргумент (1L)
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.deleteAdminCategory(1L)
        );

        // Убедимся, что при отсутствии категории eventRepository не вызывался
        verify(eventRepository, never()).existsByCategoryId(anyLong());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteAdminCategory_hasEvents_shouldThrowConflict() {
        Category category = new Category(1L, "Sport");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(
                CategoryConflictException.class,
                () -> categoryService.deleteAdminCategory(1L)
        );
    }

    @Test
    void getPublicCategories_success() {
        Category c1 = new Category(1L, "Sport");
        Category c2 = new Category(2L, "Art");

        // ВАЖНО: возвращаем List<Category>, т.к. в service вызывается findAllBy(...).stream()
        when(categoryRepository.findAllBy(any(PageRequest.class)))
                .thenReturn(List.of(c1, c2));

        List<CategoryDto> result = categoryService.getPublicCategories(0, 10);

        assertEquals(2, result.size());
        assertEquals("Sport", result.get(0).getName());
    }

    @Test
    void getPublicCategoryById_success() {
        Category category = new Category(1L, "Sport");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getPublicCategoryById(1L);

        assertEquals("Sport", result.getName());
    }

    @Test
    void getPublicCategoryById_notFound_shouldThrow() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getPublicCategoryById(1L)
        );
    }
}
















