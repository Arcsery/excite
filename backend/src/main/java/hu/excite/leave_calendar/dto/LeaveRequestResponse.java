package hu.excite.leave_calendar.dto;

import hu.excite.leave_calendar.model.LeaveStatus;
import hu.excite.leave_calendar.model.TeamMember;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeaveRequestResponse(
        Long id,
        TeamMember teamMember,
        String teamMemberName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}