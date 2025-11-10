package ru.practicum.ewm.main.service.api;

import ru.practicum.ewm.main.dto.category.CategoryDto;
import ru.practicum.ewm.main.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    // Admin. Создание Category
    CategoryDto createAdminCategory(NewCategoryDto newCategoryDto);

    // Admin. Обновление Category
    CategoryDto updateAdminCategory(Long catId, CategoryDto categoryDto);

    // Admin. Удаление Category
    void deleteAdminCategory(Long catId);

    // Public. Получение списка Category
    List<CategoryDto> getPublicCategories(Integer from, Integer size);

    // Public. Получение Category по Id
    CategoryDto getPublicCategoryById(Long catId);
}
