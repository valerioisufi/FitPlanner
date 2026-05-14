package com.example.fitplannerserver.api;

import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.controller.SessionLogController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class SessionLogBoundary {

    private final SessionLogController sessionLogController;

    public SessionLogBoundary(SessionLogController sessionLogController) {
        this.sessionLogController = sessionLogController;
    }

    // Updates the full session log with exercise results
    @PutMapping("/{sessionUuid}")
    public void updateSessionLog(@PathVariable String sessionUuid, @RequestBody SessionLogBean logBean) {
        sessionLogController.updateSessionLog(sessionUuid, logBean);
    }

    // Retrieves the full session log
    @GetMapping("/{sessionUuid}")
    public SessionLogBean getSessionLog(@PathVariable String sessionUuid) {
        return sessionLogController.getSessionLog(sessionUuid);
    }

    @GetMapping
    public List<SessionLogBean> getFilteredSessionLogs(
            @RequestParam(required = false) String athleteUuid,
            @RequestParam long startTimestamp,
            @RequestParam long endTimestamp) {
        return sessionLogController.getSessionFilteredSessionLog(athleteUuid, startTimestamp, endTimestamp);
    }

    @GetMapping("/history/exercises/{exerciseUuid}")
    public com.example.fitplannercommon.LastUsedWeightBean getLastWeightUsed(@PathVariable String exerciseUuid) {
        return logController.getLastRecordForExercise(exerciseUuid);
    }
}