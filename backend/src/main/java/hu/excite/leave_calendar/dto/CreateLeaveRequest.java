package hu.excite.leave_calendar.dto;

import hu.excite.leave_calendar.model.TeamMember;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequest(

        @NotNull(message = "Team member is required")
        TeamMember teamMember,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @FutureOrPresent(message = "End date cannot be in the past")
        LocalDate endDate,

        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason cannot be longer than 500 characters")
        String reason
) {
}