package com.example.fitplannerserver.api;

import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.controller.ProfileController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileBoundary {

    private final ProfileController profileController;

    public ProfileBoundary(ProfileController profileController) {
        this.profileController = profileController;
    }

    // Fetches the profile of the currently authenticated user
    @GetMapping("/me")
    public ProfileDTO getProfileInfo() {
        return profileController.getProfileInfo();
    }

    // Updates the profile of the currently authenticated user
    @PutMapping("/me")
    public void updateProfileInfo(@RequestBody ProfileDTO profileDTO) {
        profileController.updateProfileInfo(profileDTO);
    }

    // Athlete fetches their assigned trainer's profile
    @GetMapping("/my-trainer")
    public ProfileDTO getMyTrainer() {
        return profileController.getMyTrainer();
    }

    // Trainer fetches a list of all their subscribed athletes
    @GetMapping("/my-athletes")
    public List<ProfileDTO> getMyAthletes() {
        return profileController.getMyAthletes();
    }

    @PostMapping("/my-trainer/link")
    public void linkTrainerWithCode(@RequestBody InvitationCodeDTO invitationBean) {
        profileController.linkTrainer(invitationBean);
    }

    // Trainer requests to get their invitation code
    @PostMapping("/my-code")
    public InvitationCodeDTO getCode() {
        return profileController.getInvitationCode();
    }

}