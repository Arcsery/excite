package hu.excite.leave_calendar.dto;

import hu.excite.leave_calendar.model.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLeaveStatusRequest(

        @NotNull(message = "Status is required")
        LeaveStatus status
) {
}