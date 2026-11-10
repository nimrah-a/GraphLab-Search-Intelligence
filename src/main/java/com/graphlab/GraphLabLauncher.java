package com.graphlab;

import com.graphlab.ui.GraphLabApp;
import javafx.application.Application;

/**
 * Standard Java entry point for IntelliJ IDEA.
 *
 * Run this class (not GraphLabApp) to launch the Search Intelligence JavaFX dashboard
 * with the JavaFX dependencies provided by the Maven project.
 */
public final class GraphLabLauncher {
    private GraphLabLauncher() {
        // No instances; this class is only the application's entry point.
    }

    public static void main(String[] args) {
        Application.launch(GraphLabApp.class, args);
    }
}
