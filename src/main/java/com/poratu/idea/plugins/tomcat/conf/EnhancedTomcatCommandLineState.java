package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.application.ApplicationManager;
import com.poratu.idea.plugins.tomcat.logging.TomcatDeploymentLogger;
import com.poratu.idea.plugins.tomcat.runner.DevTomcatProcessHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Enhanced Tomcat command line state with professional deployment logging
 * Phase 1: Enhanced logging with Ultimate-style messages
 *
 * Author: Gezahegn Lemma (Gezu)
 * Project: DevTomcat Plugin
 * Created: 6/9/25
 */
public class EnhancedTomcatCommandLineState extends TomcatCommandLineState {

	private final TomcatDeploymentLogger deploymentLogger;
	private final long creationTime;

	public EnhancedTomcatCommandLineState(@NotNull ExecutionEnvironment environment,
										  TomcatRunConfiguration configuration) {
		super(environment, configuration);
		this.deploymentLogger = new TomcatDeploymentLogger(environment.getProject());
		this.creationTime = System.currentTimeMillis();

		// TEMPORARY DEBUG - remove after testing
		System.out.println("🚀 EnhancedTomcatCommandLineState is being used!");
	}

	@Override
	@NotNull
	protected OSProcessHandler startProcess() throws ExecutionException {
		long startTime = System.currentTimeMillis();

		try {
			// Get artifact name for logging
			String artifactName = getArtifactName();

			// Log deployment start - Ultimate style
			deploymentLogger.logServerConnection();
			deploymentLogger.logDeploymentStart(artifactName);

			// Create enhanced process handler with intelligent log parsing
			DevTomcatProcessHandler processHandler = new DevTomcatProcessHandler(
					createCommandLine().createProcess(),
					createCommandLine().getCommandLineString(),
					createCommandLine().getCharset(),
					deploymentLogger,
					getConfiguration()
			);

			// Set up console integration (delayed until console is available)
			setupConsoleIntegrationDelayed(processHandler);

			// Attach process termination listener
			ProcessTerminatedListener.attach(processHandler);

			// Log enhanced process creation
			long duration = System.currentTimeMillis() - startTime;
			deploymentLogger.logServerInfo("Enhanced process handler created in " + duration + " ms");

			System.out.println("DevTomcat: Enhanced process handler created successfully");
			return processHandler;

		} catch (Exception e) {
			deploymentLogger.logDeploymentError(getArtifactName(), e);
			System.err.println("DevTomcat: Failed to start process - " + e.getMessage());
			throw new ExecutionException("Failed to start Tomcat server: " + e.getMessage(), e);
		}
	}

	/**
	 * Set up console integration for enhanced logging (delayed approach)
	 */
	private void setupConsoleIntegrationDelayed(DevTomcatProcessHandler processHandler) {
		// Use invokeLater to ensure console is available when we try to access it
		ApplicationManager.getApplication().invokeLater(() -> {
			try {
				setupConsoleIntegration();
			} catch (Exception e) {
				deploymentLogger.logServerWarning("Delayed console integration failed: " + e.getMessage());
			}
		});
	}

	/**
	 * Set up console integration for enhanced logging
	 */
	private void setupConsoleIntegration() {
		try {
			// Try to get console view from RunContentManager
			RunContentManager contentManager = RunContentManager.getInstance(getEnvironment().getProject());
			RunContentDescriptor descriptor = contentManager.getSelectedContent();

			if (descriptor != null) {
				ConsoleView consoleView = descriptor.getExecutionConsole() instanceof ConsoleView ?
						(ConsoleView) descriptor.getExecutionConsole() : null;

				if (consoleView != null) {
					deploymentLogger.setConsoleView(consoleView);
					deploymentLogger.logServerInfo("Console integration enabled");
					return;
				}
			}

			// Fallback approach - console will be set later by the framework
			deploymentLogger.logServerInfo("Console integration will be enabled when console becomes available");

		} catch (Exception e) {
			deploymentLogger.logServerWarning("Console integration setup failed: " + e.getMessage());
		}
	}

	/**
	 * Alternative method: Set console view externally
	 * This can be called from the execution framework when console becomes available
	 */
	public void setConsoleView(ConsoleView consoleView) {
		if (consoleView != null) {
			deploymentLogger.setConsoleView(consoleView);
			deploymentLogger.logServerInfo("Console view set externally - enhanced logging enabled");
		}
	}

	/**
	 * Get artifact name for enhanced logging
	 */
	private String getArtifactName() {
		try {
			TomcatRunConfiguration config = getConfiguration();
			if (config != null) {
				String contextPath = config.getContextPath();
				if (contextPath != null && !contextPath.isEmpty()) {
					return contextPath.replaceFirst("^/", "") + ":war exploded";
				}
			}
			return "webapp:war exploded";
		} catch (Exception e) {
			return "unknown-artifact:war exploded";
		}
	}

	/**
	 * Get the configuration (helper method)
	 */
	private TomcatRunConfiguration getConfiguration() {
		try {
			// Access the configuration from parent class
			return (TomcatRunConfiguration) getEnvironment().getRunProfile();
		} catch (Exception e) {
			deploymentLogger.logServerWarning("Could not access run configuration: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Get the deployment logger for external access
	 */
	public TomcatDeploymentLogger getDeploymentLogger() {
		return deploymentLogger;
	}

	/**
	 * Test enhanced logging (temporary - remove after verification)
	 */
	public void testPhase1Logging() {
		System.out.println("=== DevTomcat Phase 1 Enhanced Logging Test ===");

		deploymentLogger.logServerConnection();
		deploymentLogger.logDeploymentStart("phase1-test:war exploded");
		deploymentLogger.logServerInfo("Phase 1 enhanced logging is active!");
		deploymentLogger.logServerStartup(1500);
		deploymentLogger.logDeploymentSuccess("phase1-test:war exploded", 2000);

		System.out.println("=== Phase 1 Test Complete ===");
	}

	/**
	 * Enable quick testing by calling the test method
	 */
	public void enableTestMode() {
		ApplicationManager.getApplication().invokeLater(this::testPhase1Logging);
	}

	/**
	 * Get creation time for performance monitoring
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Get performance info
	 */
	public String getPerformanceInfo() {
		long uptime = System.currentTimeMillis() - creationTime;
		return "DevTomcat uptime: " + uptime + " ms";
	}
}