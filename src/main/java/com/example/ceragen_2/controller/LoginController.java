package com.example.ceragen_2.controller;

import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, complete todos los campos");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        if (AuthService.getInstance().login(username, password)) {
            logger.info("Login exitoso para usuario: {}", username);
            ViewNavigator.getInstance().showMainView();
        } else {
            showError("Usuario o contraseña incorrectos");
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
