package hu.excite.leave_calendar.service;

import hu.excite.leave_calendar.dto.OnCallWeekResponse;
import hu.excite.leave_calendar.entity.LeaveRequest;
import hu.excite.leave_calendar.model.LeaveStatus;
import hu.excite.leave_calendar.model.TeamMember;
import hu.excite.leave_calendar.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OnCallService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestService leaveRequestService;
    private final LocalDate rotationStartDate;
    private final List<TeamMember> onCallRotationMembers;

    @Transactional(readOnly = true)
    public List<OnCallWeekResponse> getOnCallSchedule(LocalDate from, int weeks) {
        LocalDate firstWeekStart = startOfWeek(from);

        return IntStream.range(0, weeks)
                .mapToObj(index -> buildWeekResponse(firstWeekStart.plusWeeks(index)))
                .toList();
    }

    private OnCallWeekResponse buildWeekResponse(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        TeamMember onCallMember = resolveOnCallMember(weekStart);

        List<LeaveRequest> conflicts = leaveRequestRepository.findOverlappingLeavesForWeek(
                onCallMember,
                LeaveStatus.APPROVED,
                weekStart,
                weekEnd
        );

        return new OnCallWeekResponse(
                weekStart,
                weekEnd,
                onCallMember,
                onCallMember.displayName(),
                !conflicts.isEmpty(),
                conflicts.stream()
                        .map(leaveRequestService::toResponse)
                        .toList()
        );
    }

    private TeamMember resolveOnCallMember(LocalDate weekStart) {
        long weeksBetween = ChronoUnit.WEEKS.between(
                startOfWeek(rotationStartDate),
                startOfWeek(weekStart)
        );

        int rotationIndex = Math.floorMod((int) weeksBetween, onCallRotationMembers.size());
        return onCallRotationMembers.get(rotationIndex);
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}