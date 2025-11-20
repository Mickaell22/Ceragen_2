package com.example.ceragen_2;

import javafx.application.Application;

public final class Launcher {
    private Launcher() {
        // Constructor privado para prevenir instanciación
    }

    public static void main(final String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
