package hu.excite.leave_calendar.repository;

import hu.excite.leave_calendar.entity.LeaveRequest;
import hu.excite.leave_calendar.model.LeaveStatus;
import hu.excite.leave_calendar.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findAllByOrderByStartDateAsc();

    List<LeaveRequest> findByTeamMemberOrderByStartDateAsc(TeamMember teamMember);

    @Query("""
            select count(l) > 0
            from LeaveRequest l
            where l.teamMember = :teamMember
              and l.status in :statuses
              and l.startDate <= :endDate
              and l.endDate >= :startDate
            """)
    boolean hasOverlappingLeave(
            @Param("teamMember") TeamMember teamMember,
            @Param("statuses") Collection<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select l
            from LeaveRequest l
            where l.teamMember = :teamMember
              and l.status = :status
              and l.startDate <= :weekEnd
              and l.endDate >= :weekStart
            order by l.startDate asc
            """)
    List<LeaveRequest> findOverlappingLeavesForWeek(
            @Param("teamMember") TeamMember teamMember,
            @Param("status") LeaveStatus status,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );
}