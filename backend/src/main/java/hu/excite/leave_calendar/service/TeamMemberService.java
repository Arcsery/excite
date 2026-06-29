package hu.excite.leave_calendar.service;

import hu.excite.leave_calendar.model.TeamMember;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TeamMemberService {

    public List<TeamMember> getTeamMembers() {
        return Arrays.asList(TeamMember.values());
    }
}