package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.dto.State;
import ru.practicum.ewm.main.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Admin. Получение списка Event по параметрам
    @Query("""
                SELECT e FROM Event e
                WHERE (:users IS NULL OR e.initiator.id IN :users)
                  AND (:states IS NULL OR e.state IN :states)
                  AND (:categories IS NULL OR e.category.id IN :categories)
                  AND (coalesce(:rangeStart, e.eventDate) <= e.eventDate)
                  AND (coalesce(:rangeEnd, e.eventDate) >= e.eventDate)
            """)
    Page<Event> findEventsByAdminFilters(
            @Param("users") List<Long> users,
            @Param("states") List<State> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    // Public. Получение списка Event по всем параметрам
    @Query("""
                SELECT e FROM Event e
                WHERE e.state = 'PUBLISHED'
                  AND (
                  :text IS NULL
                  OR LOWER(CAST(e.annotation AS text)) LIKE LOWER(CONCAT('%', :text, '%'))
                  OR LOWER(CAST(e.description AS text)) LIKE LOWER(CONCAT('%', :text, '%'))
                  )
                  AND (:categories IS NULL OR e.category.id IN :categories)
                  AND (:paid IS NULL OR e.paid = :paid)
                  AND (e.eventDate >= :rangeStart)
                  AND (e.eventDate <= :rangeEnd)
                  AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)
            """)
    Page<Event> searchPublicEventsAllParam(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    // Public. Получение списка Event по параметрам
    @Query("""
                SELECT e FROM Event e
                WHERE (e.state = 'PUBLISHED')
                  AND (:categories IS NULL OR e.category.id IN :categories)
                  AND (:paid IS NULL OR e.paid = :paid)
                  AND (:onlyAvailable = false OR e.confirmedRequests < e.participantLimit)
            """)
    Page<Event> searchPublicEvents(
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    // Получение списка событий пользователя
    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    // Получение события по id и инициатору
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    // Проверка наличие Category в Event
    boolean existsByCategoryId(Long catId);

    // Найти событие с самым ранним createdOn (временем создания)
    Optional<Event> findFirstByOrderByCreatedOnAsc();

    // Найти событие с самым поздним createdOn (временем создания)
    Optional<Event> findFirstByOrderByCreatedOnDesc();

}
