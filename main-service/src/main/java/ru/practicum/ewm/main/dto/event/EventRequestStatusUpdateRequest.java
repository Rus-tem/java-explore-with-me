package ru.practicum.ewm.main.dto.event;

import lombok.*;
import ru.practicum.ewm.main.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;


}
