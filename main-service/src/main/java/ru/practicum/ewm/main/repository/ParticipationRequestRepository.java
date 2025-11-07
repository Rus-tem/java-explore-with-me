package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.ParticipationRequest;

import java.util.List;

 public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

 List<ParticipationRequest> findAllByEventId(Long eventId);

 // List<ParticipationRequest> findAllByUserId(Long userId);

}
