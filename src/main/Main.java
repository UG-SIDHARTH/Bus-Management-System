package main;

import gui.LoginFrame;
import web.WebServer;

import java.awt.GraphicsEnvironment;

public class Main {

    public static void main(String[] args) {
        // If explicitly invoked with --gui and display is available, launch Swing GUI
        boolean wantsGui = false;
        for (String arg : args) {
            if ("--gui".equalsIgnoreCase(arg) || "-gui".equalsIgnoreCase(arg)) {
                wantsGui = true;
                break;
            }
        }

        if (wantsGui && !GraphicsEnvironment.isHeadless()) {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        } else {
            // Default: Launch Pure Java Web Server (Website on port 6969)
            WebServer.main(args);
        }
    }
}