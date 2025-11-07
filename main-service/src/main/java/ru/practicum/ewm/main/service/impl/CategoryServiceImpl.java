package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.dto.category.CategoryDto;
import ru.practicum.ewm.main.dto.category.NewCategoryDto;
import ru.practicum.ewm.main.exception.CategoryConflictException;
import ru.practicum.ewm.main.exception.CategoryNotFoundException;
import ru.practicum.ewm.main.exception.CategoryValidationException;
import ru.practicum.ewm.main.mapper.CategoryMapper;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.api.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    // Admin. Создание Category
    @Override
    public CategoryDto createAdminCategory(NewCategoryDto newCategoryDto) {
        checkCategoryName(newCategoryDto.getName());
        checkDuplicateCategoryName(newCategoryDto.getName());
        Category category = CategoryMapper.mapNewCategoryDtoToCategory(newCategoryDto);
        categoryRepository.save(category);

        return CategoryMapper.mapToCategoryDto(category);
    }

    // Admin. Обновление Category
    @Override
    public CategoryDto updateAdminCategory(Long catId, CategoryDto categoryDto) {

        Category oldCategory = categoryRepository.findById(catId).orElseThrow(() ->
                new CategoryNotFoundException("Category с таким Id = " + catId + " не найдена"));
        if (!categoryDto.getName().equals(oldCategory.getName())) {
            checkCategoryName(categoryDto.getName());
            checkDuplicateCategoryName(categoryDto.getName());
        }
        Category newCategory = new Category(oldCategory.getId(), categoryDto.getName());
        categoryRepository.save(newCategory);

        return CategoryMapper.mapToCategoryDto(newCategory);
    }

    // Admin. Удаление Category
    @Override
    public void deleteAdminCategory(Long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new CategoryNotFoundException("Category с таким id = " + catId + "не найдена"));
        checkCategoryEvent(catId);

        categoryRepository.delete(category);
    }

    // Public. Получение списка Category
    @Override
    public List<CategoryDto> getPublicCategories(Integer from, Integer size) {
        int page = from / size; // считаем страницу
        return categoryRepository.findAllBy(PageRequest.of(page, size))
                .stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    // Public. Получение Category по Id
    @Override
    public CategoryDto getPublicCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new CategoryNotFoundException("Category с таким id = " + catId + "не найдена"));
        return CategoryMapper.mapToCategoryDto(category);
    }

    // Метод для проверки имени Category
    private void checkCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank() || categoryName.length() > 50) {
            throw new CategoryValidationException("Некорректное имя Category");
        }
    }

    // Метод для проверки дубля Category
    private void checkDuplicateCategoryName(String categoryName) {
        if (categoryRepository.existsByName(categoryName)) {
            throw new CategoryConflictException("Category с таким именем уже существует");
        }
    }

    // Метод проверки Event в Category
    private void checkCategoryEvent(Long catId) {

        if (eventRepository.existsByCategoryId(catId)) {
            throw new CategoryConflictException("К данной Category " + catId + " привязано событие");
        }
    }
}
