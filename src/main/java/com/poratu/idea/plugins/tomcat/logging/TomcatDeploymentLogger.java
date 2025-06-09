package com.poratu.idea.plugins.tomcat.logging;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Complete Enhanced Deployment Logger with Ultimate-style console output
 * Provides professional deployment logging and structured console messages
 *
 * Author: Gezahegn Lemma (Gezu)
 * Project: Dev Tomcat Plugin
 * Created: 6/9/25
 */
public class TomcatDeploymentLogger {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");

    private final Project project;
    private ConsoleView consoleView;

    public TomcatDeploymentLogger(@NotNull Project project) {
        this.project = project;
        System.out.println("DevTomcat: TomcatDeploymentLogger created for project: " + project.getName());
    }

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
        System.out.println("DevTomcat: Console view set for enhanced logging");
    }

    /**
     * Log server connection established
     */
    public void logServerConnection() {
        String message = "Connected to server\n";
        printToConsole(message, ConsoleViewContentType.SYSTEM_OUTPUT);
        System.out.println("DevTomcat: Server connection logged");
    }

    /**
     * Log deployment start with Ultimate-style message
     */
    public void logDeploymentStart(String artifactName) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String message = String.format("[%s] Artifact %s: Artifact is being deployed, please wait...\n",
                timestamp, artifactName);
        printToConsole(message, ConsoleViewContentType.SYSTEM_OUTPUT);
        System.out.println("DevTomcat: Deployment start logged for: " + artifactName);
    }

    /**
     * Log deployment success with Ultimate-style message
     */
    public void logDeploymentSuccess(String artifactName, long durationMs) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String message = String.format("[%s] Artifact %s: Artifact is deployed successfully\n",
                timestamp, artifactName);
        printToConsole(message, ConsoleViewContentType.NORMAL_OUTPUT);
        System.out.println("DevTomcat: Deployment success logged for: " + artifactName + " in " + durationMs + "ms");
    }

    /**
     * Log deployment error with details
     */
    public void logDeploymentError(String artifactName, Exception e) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String message = String.format("[%s] Artifact %s: Deployment failed - %s\n",
                timestamp, artifactName, e.getMessage());
        printToConsole(message, ConsoleViewContentType.ERROR_OUTPUT);
        System.err.println("DevTomcat: Deployment error logged for: " + artifactName + " - " + e.getMessage());
    }

    /**
     * Log server startup time
     */
    public void logServerStartup(long startupTimeMs) {
        String message = String.format("Server startup in %d ms\n", startupTimeMs);
        printToConsole(message, ConsoleViewContentType.NORMAL_OUTPUT);
        System.out.println("DevTomcat: Server startup logged: " + startupTimeMs + "ms");
    }

    /**
     * Log server information messages
     */
    public void logServerInfo(String message) {
        String formatted = formatMessage("INFO", message);
        printToConsole(formatted, ConsoleViewContentType.NORMAL_OUTPUT);
        System.out.println("DevTomcat: Server info - " + message);
    }

    /**
     * Log server warning messages
     */
    public void logServerWarning(String message) {
        String formatted = formatMessage("WARN", message);
        printToConsole(formatted, ConsoleViewContentType.ERROR_OUTPUT);
        System.out.println("DevTomcat: Server warning - " + message);
    }

    /**
     * Log server error messages
     */
    public void logServerError(String message) {
        String formatted = formatMessage("ERROR", message);
        printToConsole(formatted, ConsoleViewContentType.ERROR_OUTPUT);
        System.err.println("DevTomcat: Server error - " + message);
    }

    /**
     * Log general information messages
     */
    public void logInfo(String message) {
        String formatted = formatMessage("INFO", message);
        printToConsole(formatted, ConsoleViewContentType.NORMAL_OUTPUT);
        System.out.println("DevTomcat: Info - " + message);
    }

    /**
     * Log warning messages
     */
    public void logWarning(String message) {
        String formatted = formatMessage("WARN", message);
        printToConsole(formatted, ConsoleViewContentType.ERROR_OUTPUT);
        System.out.println("DevTomcat: Warning - " + message);
    }

    /**
     * Log error messages
     */
    public void logError(String message) {
        String formatted = formatMessage("ERROR", message);
        printToConsole(formatted, ConsoleViewContentType.ERROR_OUTPUT);
        System.err.println("DevTomcat: Error - " + message);
    }

    /**
     * Format message with timestamp and level
     */
    private String formatMessage(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        return String.format("[%s] %s: %s\n", timestamp, level, message);
    }

    /**
     * Print message to console with proper content type
     */
    private void printToConsole(String message, ConsoleViewContentType contentType) {
        if (consoleView != null) {
            try {
                consoleView.print(message, contentType);
            } catch (Exception e) {
                System.err.println("DevTomcat: Error printing to console: " + e.getMessage());
                // Fallback to system output
                System.out.print(message);
            }
        } else {
            // Fallback to system output if console not available
            System.out.print(message);
        }
    }

    /**
     * Get the associated project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Test logging functionality for debugging
     */
    public void testLogging() {
        System.out.println("DevTomcat: Starting logging test...");

        logServerConnection();
        logDeploymentStart("test-app:war exploded");
        logServerInfo("Test server information message");
        logServerStartup(1500);
        logDeploymentSuccess("test-app:war exploded", 2000);
        logServerWarning("Test warning message");

        System.out.println("DevTomcat: Logging test completed");
    }

    /**
     * Check if console is available
     */
    public boolean isConsoleAvailable() {
        return consoleView != null;
    }

    /**
     * Clear console if available
     */
    public void clearConsole() {
        if (consoleView != null) {
            try {
                consoleView.clear();
                System.out.println("DevTomcat: Console cleared");
            } catch (Exception e) {
                System.err.println("DevTomcat: Error clearing console: " + e.getMessage());
            }
        }
    }
}