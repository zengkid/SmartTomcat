package com.poratu.idea.plugins.tomcat.logging;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Phase 1: Enhanced deployment logger with Ultimate-like status messages
 */
public class DevTomcatDeploymentLogger {

    private static final DateTimeFormatter TIME_FORMAT =
	    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    private final Project project;
    private ConsoleView consoleView;

    public DevTomcatDeploymentLogger(@NotNull Project project) {
	this.project = project;
    }

    public void setConsoleView(ConsoleView consoleView) {
	this.consoleView = consoleView;
    }

    /**
     * Log deployment start - Ultimate style
     */
    public void logDeploymentStart(String artifactName) {
	String timestamp = getCurrentTimestamp();
	String message = String.format("[%s] Artifact %s: Artifact is being deployed, please wait...%n",
		timestamp, artifactName);
	printToConsole(message, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    /**
     * Log deployment success - Ultimate style
     */
    public void logDeploymentSuccess(String artifactName, long durationMs) {
	String timestamp = getCurrentTimestamp();
	String message = String.format("[%s] Artifact %s: Artifact is deployed successfully%n",
		timestamp, artifactName);
	printToConsole(message, ConsoleViewContentType.NORMAL_OUTPUT);

	// Also log the timing information
	logServerInfo(String.format("✅ Deployment completed in %d ms", durationMs));
    }

    /**
     * Log deployment error - Ultimate style
     */
    public void logDeploymentError(String artifactName, String errorMessage) {
	String timestamp = getCurrentTimestamp();
	String message = String.format("[%s] Artifact %s: Error during artifact deployment. %s%n",
		timestamp, artifactName, errorMessage);
	printToConsole(message, ConsoleViewContentType.ERROR_OUTPUT);
    }

    /**
     * Log server connection status
     */
    public void logServerConnection() {
	printToConsole("Connected to server%n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    /**
     * Log server startup completion
     */
    public void logServerStartup(long startupTimeMs) {
	String message = String.format("Server startup in %d ms%n", startupTimeMs);
	printToConsole(message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * Log general server information
     */
    public void logServerInfo(String message) {
	String formattedMessage = String.format("%s%n", message);
	printToConsole(formattedMessage, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * Log server warnings
     */
    public void logServerWarning(String message) {
	String formattedMessage = String.format("%s%n", message);
	printToConsole(formattedMessage, ConsoleViewContentType.ERROR_OUTPUT);
    }

    /**
     * Log server errors
     */
    public void logServerError(String message) {
	String formattedMessage = String.format("%s%n", message);
	printToConsole(formattedMessage, ConsoleViewContentType.ERROR_OUTPUT);
    }

    private String getCurrentTimestamp() {
	return LocalDateTime.now().format(TIME_FORMAT);
    }

    private void printToConsole(String message, ConsoleViewContentType contentType) {
	if (consoleView != null) {
	    ApplicationManager.getApplication().invokeLater(() -> {
		consoleView.print(message, contentType);
	    });
	}
    }
}