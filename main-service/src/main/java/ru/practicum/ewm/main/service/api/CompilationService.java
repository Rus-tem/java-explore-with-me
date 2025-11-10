package ru.practicum.ewm.main.service.api;

import ru.practicum.ewm.main.dto.compilation.CompilationDto;
import ru.practicum.ewm.main.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    // Admin. Создание Compilation
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    // Admin. Обновление Compilation
    void deleteCompilation(Long compId);

    // Admin. Удаление Compilation
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    // Public. Получение списка Compilation
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    // Public. Получение Compilation по Id
    CompilationDto getCompilationById(Long compId);
}
