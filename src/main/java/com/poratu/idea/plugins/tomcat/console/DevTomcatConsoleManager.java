package com.poratu.idea.plugins.tomcat.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simplified Phase 1: Console manager without complex tab creation
 * Focuses on console view management for enhanced logging
 */
public class DevTomcatConsoleManager {

    private final Project project;
    private final Map<String, ConsoleView> consoleViews = new ConcurrentHashMap<>();
    private ConsoleView mainConsoleView;

    public DevTomcatConsoleManager(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Set the main console view (provided externally by the run framework)
     */
    public void setMainConsoleView(@NotNull ConsoleView consoleView) {
        this.mainConsoleView = consoleView;
        consoleViews.put("Server", consoleView);
    }

    /**
     * Get main server console
     */
    @Nullable
    public ConsoleView getServerConsole() {
        return mainConsoleView;
    }

    /**
     * Register a console view externally
     */
    public void registerConsoleView(@NotNull String name, @NotNull ConsoleView consoleView) {
        consoleViews.put(name, consoleView);
    }

    /**
     * Get console for specific tab
     */
    @Nullable
    public ConsoleView getConsole(String tabName) {
        return consoleViews.get(tabName);
    }

    /**
     * Print message to specific console tab
     */
    public void printToConsole(String tabName, String message, ConsoleViewContentType contentType) {
        ConsoleView console = getConsole(tabName);
        if (console != null) {
            console.print(message, contentType);
        }
    }

    /**
     * Print to server console (main tab)
     */
    public void printToServerConsole(String message, ConsoleViewContentType contentType) {
        if (mainConsoleView != null) {
            mainConsoleView.print(message, contentType);
        }
    }

    /**
     * Print deployment message with enhanced formatting
     */
    public void printDeploymentMessage(String message) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
        String formattedMessage = String.format("[%s] %s%n", timestamp, message);
        printToServerConsole(formattedMessage, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    /**
     * Print error message with enhanced formatting
     */
    public void printErrorMessage(String message) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
        String formattedMessage = String.format("[%s] ERROR: %s%n", timestamp, message);
        printToServerConsole(formattedMessage, ConsoleViewContentType.ERROR_OUTPUT);
    }

    /**
     * Clear main console
     */
    public void clearConsole() {
        if (mainConsoleView != null) {
            mainConsoleView.clear();
        }
    }

    /**
     * Check if main console is available
     */
    public boolean hasMainConsole() {
        return mainConsoleView != null;
    }

    /**
     * Get project instance
     */
    @NotNull
    public Project getProject() {
        return project;
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        consoleViews.clear();
        mainConsoleView = null;
    }



}