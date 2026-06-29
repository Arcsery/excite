package hu.excite.leave_calendar.config;

import hu.excite.leave_calendar.model.TeamMember;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class OnCallRotationConfig {

    @Bean
    public LocalDate rotationStartDate() {
        return LocalDate.of(2026, 1, 5);
    }

    @Bean
    public List<TeamMember> onCallRotationMembers() {
        return List.of(
                TeamMember.ALICE,
                TeamMember.BOB,
                TeamMember.CHARLIE,
                TeamMember.DIANA
        );
    }
}