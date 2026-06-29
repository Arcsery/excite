package hu.excite.leave_calendar.service;

import hu.excite.leave_calendar.dto.OnCallWeekResponse;
import hu.excite.leave_calendar.entity.LeaveRequest;
import hu.excite.leave_calendar.model.LeaveStatus;
import hu.excite.leave_calendar.model.TeamMember;
import hu.excite.leave_calendar.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OnCallServiceTest {

    private LeaveRequestRepository leaveRequestRepository;
    private LeaveRequestService leaveRequestService;
    private OnCallService onCallService;

    @BeforeEach
    void setUp() {
        leaveRequestRepository = Mockito.mock(LeaveRequestRepository.class);
        leaveRequestService = new LeaveRequestService(leaveRequestRepository);

        onCallService = new OnCallService(
                leaveRequestRepository,
                leaveRequestService,
                LocalDate.of(2026, 1, 5),
                List.of(
                        TeamMember.ALICE,
                        TeamMember.BOB,
                        TeamMember.CHARLIE,
                        TeamMember.DIANA
                )
        );
    }

    @Test
    void shouldReturnFourWeekRotationFromRotationStartDate() {
        when(leaveRequestRepository.findOverlappingLeavesForWeek(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeekResponse> schedule = onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 5),
                4
        );

        assertThat(schedule).hasSize(4);

        assertThat(schedule.get(0).weekStart()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(schedule.get(0).weekEnd()).isEqualTo(LocalDate.of(2026, 1, 11));
        assertThat(schedule.get(0).onCallMember()).isEqualTo(TeamMember.ALICE);
        assertThat(schedule.get(0).conflict()).isFalse();

        assertThat(schedule.get(1).onCallMember()).isEqualTo(TeamMember.BOB);
        assertThat(schedule.get(2).onCallMember()).isEqualTo(TeamMember.CHARLIE);
        assertThat(schedule.get(3).onCallMember()).isEqualTo(TeamMember.DIANA);
    }

    @Test
    void shouldRepeatRotationAfterFourWeeks() {
        when(leaveRequestRepository.findOverlappingLeavesForWeek(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeekResponse> schedule = onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 5),
                5
        );

        assertThat(schedule).hasSize(5);
        assertThat(schedule.get(0).onCallMember()).isEqualTo(TeamMember.ALICE);
        assertThat(schedule.get(1).onCallMember()).isEqualTo(TeamMember.BOB);
        assertThat(schedule.get(2).onCallMember()).isEqualTo(TeamMember.CHARLIE);
        assertThat(schedule.get(3).onCallMember()).isEqualTo(TeamMember.DIANA);
        assertThat(schedule.get(4).onCallMember()).isEqualTo(TeamMember.ALICE);
    }

    @Test
    void shouldNormalizeInputDateToMondayStartOfWeek() {
        when(leaveRequestRepository.findOverlappingLeavesForWeek(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OnCallWeekResponse> schedule = onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 7),
                1
        );

        assertThat(schedule).hasSize(1);
        assertThat(schedule.getFirst().weekStart()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(schedule.getFirst().weekEnd()).isEqualTo(LocalDate.of(2026, 1, 11));
        assertThat(schedule.getFirst().onCallMember()).isEqualTo(TeamMember.ALICE);
    }

    @Test
    void shouldShowConflictWhenOnCallPersonHasApprovedLeaveDuringThatWeek() {
        LeaveRequest approvedLeave = leave(
                1L,
                TeamMember.ALICE,
                LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 8),
                "Medical leave",
                LeaveStatus.APPROVED
        );

        when(leaveRequestRepository.findOverlappingLeavesForWeek(
                TeamMember.ALICE,
                LeaveStatus.APPROVED,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 11)
        )).thenReturn(List.of(approvedLeave));

        List<OnCallWeekResponse> schedule = onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 5),
                1
        );

        OnCallWeekResponse week = schedule.getFirst();

        assertThat(week.onCallMember()).isEqualTo(TeamMember.ALICE);
        assertThat(week.conflict()).isTrue();
        assertThat(week.conflictingLeaveRequests()).hasSize(1);
        assertThat(week.conflictingLeaveRequests().getFirst().teamMember()).isEqualTo(TeamMember.ALICE);
        assertThat(week.conflictingLeaveRequests().getFirst().status()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    void shouldNotShowConflictWhenNoApprovedLeaveExists() {
        when(leaveRequestRepository.findOverlappingLeavesForWeek(
                TeamMember.ALICE,
                LeaveStatus.APPROVED,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 11)
        )).thenReturn(List.of());

        List<OnCallWeekResponse> schedule = onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 5),
                1
        );

        OnCallWeekResponse week = schedule.getFirst();

        assertThat(week.onCallMember()).isEqualTo(TeamMember.ALICE);
        assertThat(week.conflict()).isFalse();
        assertThat(week.conflictingLeaveRequests()).isEmpty();
    }

    @Test
    void shouldQueryApprovedLeavesOnlyForConflictDetection() {
        when(leaveRequestRepository.findOverlappingLeavesForWeek(any(), any(), any(), any()))
                .thenReturn(List.of());

        onCallService.getOnCallSchedule(
                LocalDate.of(2026, 1, 5),
                1
        );

        verify(leaveRequestRepository).findOverlappingLeavesForWeek(
                TeamMember.ALICE,
                LeaveStatus.APPROVED,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 11)
        );
    }

    private LeaveRequest leave(
            Long id,
            TeamMember teamMember,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            LeaveStatus status
    ) {
        return LeaveRequest.builder()
                .id(id)
                .teamMember(teamMember)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .status(status)
                .build();
    }
}