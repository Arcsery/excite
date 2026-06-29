package hu.excite.leave_calendar.service;

import hu.excite.leave_calendar.dto.CreateLeaveRequest;
import hu.excite.leave_calendar.dto.LeaveRequestResponse;
import hu.excite.leave_calendar.dto.UpdateLeaveStatusRequest;
import hu.excite.leave_calendar.entity.LeaveRequest;
import hu.excite.leave_calendar.exception.LeaveOverlapException;
import hu.excite.leave_calendar.exception.NotFoundException;
import hu.excite.leave_calendar.model.LeaveStatus;
import hu.excite.leave_calendar.model.TeamMember;
import hu.excite.leave_calendar.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Test
    void shouldCreateLeaveRequestWithPendingStatusWhenNoOverlapExists() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                TeamMember.ALICE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                "Summer holiday"
        );

        when(leaveRequestRepository.hasOverlappingLeave(
                eq(TeamMember.ALICE),
                any(Collection.class),
                eq(LocalDate.of(2026, 7, 6)),
                eq(LocalDate.of(2026, 7, 10))
        )).thenReturn(false);

        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest leaveRequest = invocation.getArgument(0);
                    leaveRequest.setId(1L);
                    return leaveRequest;
                });

        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.teamMember()).isEqualTo(TeamMember.ALICE);
        assertThat(response.teamMemberName()).isEqualTo("Alice");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(response.reason()).isEqualTo("Summer holiday");
        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);

        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void shouldTrimReasonWhenCreatingLeaveRequest() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                TeamMember.BOB,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                "  Family event  "
        );

        when(leaveRequestRepository.hasOverlappingLeave(any(), any(), any(), any()))
                .thenReturn(false);

        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest leaveRequest = invocation.getArgument(0);
                    leaveRequest.setId(10L);
                    return leaveRequest;
                });

        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(request);

        assertThat(response.reason()).isEqualTo("Family event");
    }

    @Test
    void shouldRejectLeaveRequestWhenEndDateIsBeforeStartDate() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                TeamMember.ALICE,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 5),
                "Invalid range"
        );

        assertThatThrownBy(() -> leaveRequestService.createLeaveRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date cannot be before start date");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void shouldRejectOverlappingLeaveRequest() {
        CreateLeaveRequest request = new CreateLeaveRequest(
                TeamMember.ALICE,
                LocalDate.of(2026, 7, 9),
                LocalDate.of(2026, 7, 12),
                "Overlapping holiday"
        );

        when(leaveRequestRepository.hasOverlappingLeave(
                eq(TeamMember.ALICE),
                any(Collection.class),
                eq(LocalDate.of(2026, 7, 9)),
                eq(LocalDate.of(2026, 7, 12))
        )).thenReturn(true);

        assertThatThrownBy(() -> leaveRequestService.createLeaveRequest(request))
                .isInstanceOf(LeaveOverlapException.class)
                .hasMessageContaining("Overlapping pending or approved leave request already exists");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void shouldReturnLeaveRequestsForAllTeamMembers() {
        LeaveRequest aliceLeave = leave(
                1L,
                TeamMember.ALICE,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "Alice leave",
                LeaveStatus.PENDING
        );

        LeaveRequest bobLeave = leave(
                2L,
                TeamMember.BOB,
                LocalDate.of(2026, 7, 4),
                LocalDate.of(2026, 7, 5),
                "Bob leave",
                LeaveStatus.APPROVED
        );

        when(leaveRequestRepository.findAllByOrderByStartDateAsc())
                .thenReturn(List.of(aliceLeave, bobLeave));

        List<LeaveRequestResponse> responses = leaveRequestService.getLeaveRequests(null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).teamMember()).isEqualTo(TeamMember.ALICE);
        assertThat(responses.get(1).teamMember()).isEqualTo(TeamMember.BOB);
    }

    @Test
    void shouldReturnLeaveRequestsFilteredByTeamMember() {
        LeaveRequest aliceLeave = leave(
                1L,
                TeamMember.ALICE,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "Alice leave",
                LeaveStatus.PENDING
        );

        when(leaveRequestRepository.findByTeamMemberOrderByStartDateAsc(TeamMember.ALICE))
                .thenReturn(List.of(aliceLeave));

        List<LeaveRequestResponse> responses = leaveRequestService.getLeaveRequests(TeamMember.ALICE);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().teamMember()).isEqualTo(TeamMember.ALICE);
    }

    @Test
    void shouldReturnLeaveRequestById() {
        LeaveRequest leaveRequest = leave(
                1L,
                TeamMember.CHARLIE,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 5),
                "Conference",
                LeaveStatus.PENDING
        );

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));

        LeaveRequestResponse response = leaveRequestService.getLeaveRequest(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.teamMember()).isEqualTo(TeamMember.CHARLIE);
        assertThat(response.reason()).isEqualTo("Conference");
    }

    @Test
    void shouldThrowNotFoundWhenLeaveRequestDoesNotExist() {
        when(leaveRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveRequestService.getLeaveRequest(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Leave request not found with id: 999");
    }

    @Test
    void shouldUpdateLeaveStatus() {
        LeaveRequest leaveRequest = leave(
                1L,
                TeamMember.DIANA,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 4),
                "Family event",
                LeaveStatus.PENDING
        );

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));

        LeaveRequestResponse response = leaveRequestService.updateStatus(
                1L,
                new UpdateLeaveStatusRequest(LeaveStatus.APPROVED)
        );

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingLeaveRequest() {
        when(leaveRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveRequestService.updateStatus(
                999L,
                new UpdateLeaveStatusRequest(LeaveStatus.APPROVED)
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Leave request not found with id: 999");
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