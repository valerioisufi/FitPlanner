package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.AuthenticationView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class AuthenticationViewController implements GuiController {
    private GuiManager guiManager;

    private final Navigator navigator;
    private final AuthManager authManager;
    private final AuthenticationView view;

    private final Runnable onLoginSuccess;

    public AuthenticationViewController(Navigator navigator, GuiManager guiManager, AuthManager authManager, Runnable onLoginSuccess) {
        this.navigator = navigator;
        this.guiManager = guiManager;
        this.authManager = authManager;

        this.view = new AuthenticationView();

        this.onLoginSuccess = onLoginSuccess;

        this.view.setLoginBtnAction(this::onLogin);
        this.view.setRegistrationBtnAction(this::onRegister);
    }

    private void onLogin() {
        LoginBean loginBean = new LoginBean(this.view.getEmail(), this.view.getPassword());

        authManager.loginAsync(loginBean) //da modificare
                .thenRun(onLoginSuccess)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        this.guiManager.showNotification(ex.getCause().getMessage());
                    });
                    return null;
                });
    }

    private void onRegister() {
        RegisterBean registerBean = new RegisterBean();
        registerBean.setEmail(this.view.getEmail());
        registerBean.setPassword(this.view.getPassword());
        registerBean.setFirstName(this.view.getFirstName());
        registerBean.setLastName(this.view.getLastName());
        registerBean.setPhoneNumber(this.view.getPhoneNumber());
        registerBean.setContactEmail(this.view.getContactEmail());


        authManager.registerAsync(registerBean)
                .thenRun(onLoginSuccess)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        this.guiManager.showNotification(ex.getCause().getMessage());
                    });
                    return null;
                });
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(){}
}