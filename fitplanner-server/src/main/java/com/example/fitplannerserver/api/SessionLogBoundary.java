package com.example.fitplannerserver.api;

import com.example.fitplannercommon.ExerciseLogBean;
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

    @PutMapping("/session")
    public void saveSessionLog(@RequestBody SessionLogBean logBean) {
        sessionLogController.saveSessionLog(logBean);
    }

    @GetMapping("/session")
    public List<SessionLogBean> getFilteredSessionLogs(
            @RequestParam(required = false) String athleteUuid,
            @RequestParam long startTimestamp,
            @RequestParam long endTimestamp) {
        return sessionLogController.getFilteredSessionLog(athleteUuid, startTimestamp, endTimestamp);
    }

    @GetMapping("/exercises/{exerciseId}/latest")
    public ExerciseLogBean getLastWeightUsed(@PathVariable String exerciseId) {
        return sessionLogController.getLastRecordForExercise(exerciseId);
    }
}