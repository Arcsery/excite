package hu.excite.leave_calendar.controller;

import hu.excite.leave_calendar.model.TeamMember;
import hu.excite.leave_calendar.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @GetMapping
    public List<TeamMember> getTeamMembers() {
        return teamMemberService.getTeamMembers();
    }
}