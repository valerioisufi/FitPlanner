package com.example.fitplannerserver.api;

import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;
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
    public void saveSessionLog(@RequestBody SessionLogDTO logBean) {
        sessionLogController.saveSessionLog(logBean);
    }

    @GetMapping("/session")
    public List<SessionLogDTO> getFilteredSessionLogs(
            @RequestParam(required = false) String athleteId,
            @RequestParam long startTimestamp,
            @RequestParam long endTimestamp) {
        return sessionLogController.getFilteredSessionLog(athleteId, startTimestamp, endTimestamp);
    }

    @GetMapping("/exercises/{exerciseId}/latest")
    public ExerciseLogDTO getLastWeightUsed(@PathVariable String exerciseId) {
        return sessionLogController.getLastRecordForExercise(exerciseId);
    }
}