package com.krzysztofpk14.app.gui;

import javafx.application.Application;

/**
 * Launcher class for the Trading Application GUI.
 * This class exists to provide a standard main method that can be used
 * to start the JavaFX application from a standard Java executable JAR.
 */
public class TradingAppLauncher {
    public static void main(String[] args) {
        Application.launch(TradingAppGUI.class, args);
    }
}