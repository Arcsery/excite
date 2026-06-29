package hu.excite.leave_calendar.controller;

import hu.excite.leave_calendar.dto.CreateLeaveRequest;
import hu.excite.leave_calendar.dto.LeaveRequestResponse;
import hu.excite.leave_calendar.dto.UpdateLeaveStatusRequest;
import hu.excite.leave_calendar.model.TeamMember;
import hu.excite.leave_calendar.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    public List<LeaveRequestResponse> getLeaveRequests(
            @RequestParam(required = false) TeamMember teamMember
    ) {
        return leaveRequestService.getLeaveRequests(teamMember);
    }

    @GetMapping("/{id}")
    public LeaveRequestResponse getLeaveRequest(@PathVariable Long id) {
        return leaveRequestService.getLeaveRequest(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequest request
    ) {
        return leaveRequestService.createLeaveRequest(request);
    }

    @PatchMapping("/{id}/status")
    public LeaveRequestResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeaveStatusRequest request
    ) {
        return leaveRequestService.updateStatus(id, request);
    }
}