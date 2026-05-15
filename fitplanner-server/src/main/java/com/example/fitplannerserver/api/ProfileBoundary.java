package com.example.fitplannerserver.api;

import com.example.fitplannercommon.InvitationCodeBean;
import com.example.fitplannercommon.ProfileBean;
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
    public ProfileBean getProfileInfo() {
        return profileController.getProfileInfo();
    }

    // Updates the profile of the currently authenticated user
    @PutMapping("/me")
    public void updateProfileInfo(@RequestBody ProfileBean profileBean) {
        profileController.updateProfileInfo(profileBean);
    }

    // Athlete fetches their assigned trainer's profile
    @GetMapping("/my-trainer")
    public ProfileBean getMyTrainer() {
        return profileController.getMyTrainer();
    }

    // Trainer fetches a list of all their subscribed athletes
    @GetMapping("/my-athletes")
    public List<ProfileBean> getMyAthletes() {
        return profileController.getMyAthletes();
    }

    @PostMapping("/my-trainer/link")
    public void linkTrainerWithCode(@RequestBody InvitationCodeBean invitationBean) {
        profileController.linkTrainer(invitationBean);
    }

    // Trainer requests to get their invitation code
    @PostMapping("/my-code")
    public InvitationCodeBean getCode() {
        return profileController.getInvitationCode();
    }

}