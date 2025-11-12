package ru.practicum.ewm.main.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(Long eventId);

    // Удалить все комментарии по eventId и userId
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.event.id = :eventId AND c.user.id = :userId")
    void deleteAllByEventIdAndUserId(@Param("eventId") Long eventId,
                                     @Param("userId") Long userId);

    // Удалить комментарии у Event
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.event.id = :eventId")
    void deleteByEvent(@Param("eventId") Long eventId);

    // (опционально) удалить конкретный comment внутри конкретного event
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.id = :commentId AND c.event.id = :eventId")
    void deleteByIdAndEventId(@Param("commentId") Long commentId,
                              @Param("eventId") Long eventId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Comment c
            WHERE c.user.id = :userId
              AND c.event.id = :eventId
            """)
    void deleteAllByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );

    @Query("""
             SELECT c
             FROM Comment c
             WHERE (:userId IS NULL OR c.user.id = :userId)
               AND (:eventId IS NULL OR c.event.id = :eventId)
               AND (:commentId IS NULL OR c.id = :commentId)
            """)
    Page<Comment> findAllByAdminParams(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId,
            @Param("commentId") Long commentId,
            Pageable pageable
    );


    @Query("""
            SELECT c
            FROM Comment c
            WHERE (:userId IS NULL OR c.user.id = :userId)
              AND (:eventId IS NULL OR c.event.id = :eventId)
              AND (:commentId IS NULL OR c.id = :commentId)
              AND (c.createdOn >= :rangeStart)
              AND (c.createdOn <= :rangeEnd)
            """)
    Page<Comment> findAllByAdminAllParams(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId,
            @Param("commentId") Long commentId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("""
             SELECT c
             FROM Comment c
             WHERE (:userId IS NULL OR c.user.id = :userId)
               AND (:eventId IS NULL OR c.event.id = :eventId)
               AND (:commentId IS NULL OR c.id = :commentId)
            """)
    List<Comment> findAllByParams(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId,
            @Param("commentId") Long commentId
    );


}
