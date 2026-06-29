package hu.excite.leave_calendar.controller;

import hu.excite.leave_calendar.dto.OnCallWeekResponse;
import hu.excite.leave_calendar.service.OnCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/on-call")
@RequiredArgsConstructor
public class OnCallController {

    private final OnCallService onCallService;

    @GetMapping
    public List<OnCallWeekResponse> getOnCallSchedule(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(defaultValue = "8") int weeks
    ) {
        LocalDate effectiveFrom = from == null ? LocalDate.now() : from;
        int effectiveWeeks = Math.clamp(weeks, 1, 52);

        return onCallService.getOnCallSchedule(effectiveFrom, effectiveWeeks);
    }
}