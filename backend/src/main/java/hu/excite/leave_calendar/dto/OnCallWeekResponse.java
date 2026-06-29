package hu.excite.leave_calendar.dto;

import hu.excite.leave_calendar.model.TeamMember;

import java.time.LocalDate;
import java.util.List;

public record OnCallWeekResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        TeamMember onCallMember,
        String onCallMemberName,
        boolean conflict,
        List<LeaveRequestResponse> conflictingLeaveRequests
) {
}