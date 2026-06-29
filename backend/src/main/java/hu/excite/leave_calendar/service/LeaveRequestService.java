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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeaveRequests(TeamMember teamMember) {
        List<LeaveRequest> leaveRequests = teamMember == null
                ? leaveRequestRepository.findAllByOrderByStartDateAsc()
                : leaveRequestRepository.findByTeamMemberOrderByStartDateAsc(teamMember);

        return leaveRequests.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        validateNoOverlap(request);

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .teamMember(request.teamMember())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reason(request.reason().trim())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return toResponse(savedLeaveRequest);
    }

    @Transactional
    public LeaveRequestResponse updateStatus(Long id, UpdateLeaveStatusRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Leave request not found with id: " + id));

        leaveRequest.setStatus(request.status());

        return toResponse(leaveRequest);
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Leave request not found with id: " + id));
    }

    public LeaveRequestResponse toResponse(LeaveRequest leaveRequest) {
        return new LeaveRequestResponse(
                leaveRequest.getId(),
                leaveRequest.getTeamMember(),
                leaveRequest.getTeamMember().displayName(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getReason(),
                leaveRequest.getStatus(),
                leaveRequest.getCreatedAt(),
                leaveRequest.getUpdatedAt()
        );
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private void validateNoOverlap(CreateLeaveRequest request) {
        boolean overlapExists = leaveRequestRepository.hasOverlappingLeave(
                request.teamMember(),
                EnumSet.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                request.startDate(),
                request.endDate()
        );

        if (overlapExists) {
            throw new LeaveOverlapException(
                    "Overlapping pending or approved leave request already exists for "
                            + request.teamMember().displayName()
            );
        }
    }
}