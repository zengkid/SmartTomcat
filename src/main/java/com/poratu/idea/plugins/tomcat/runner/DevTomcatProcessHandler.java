package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.logging.TomcatDeploymentLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Complete Enhanced Tomcat Process Handler with Intelligent Log Parsing
 * Phase 1: Integrates with TomcatDeploymentLogger for Ultimate-style output
 * Provides real-time deployment monitoring and professional console output
 *
 * Author: Gezahegn Lemma (Gezu)
 * Project: DevTomcat Plugin
 * Created: 6/9/25
 */
public class DevTomcatProcessHandler extends KillableColoredProcessHandler {

	private final TomcatDeploymentLogger deploymentLogger;
	private final EnhancedTomcatRunConfiguration configuration;
	private final long processStartTime;

	// Patterns for intelligent log parsing
	private static final Pattern SERVER_STARTUP_PATTERN = Pattern.compile(".*Server startup in (\\d+) ms.*");
	private static final Pattern DEPLOYMENT_PATTERN = Pattern.compile(".*Deploying web application.*");
	private static final Pattern DEPLOYMENT_SUCCESS_PATTERN = Pattern.compile(".*Deployment of web application directory.*has finished in (\\d+) ms.*");
	private static final Pattern ERROR_PATTERN = Pattern.compile(".*(ERROR|SEVERE|Exception|Failed).*");
	private static final Pattern WARNING_PATTERN = Pattern.compile(".*(WARN|WARNING).*");
	private static final Pattern SPRING_STARTUP_PATTERN = Pattern.compile(".*Started .* in ([\\d.]+) seconds.*");
	private static final Pattern JMX_ENABLED_PATTERN = Pattern.compile(".*JMX.*enabled.*port.*?(\\d+).*");
	private static final Pattern HOT_DEPLOY_PATTERN = Pattern.compile(".*Reloading Context with name.*");
	private static final Pattern MEMORY_PATTERN = Pattern.compile(".*Memory usage: heap (\\d+)M.*");

	public DevTomcatProcessHandler(@NotNull Process process,
								   @NotNull String commandLine,
								   @NotNull Charset charset,
								   @NotNull TomcatDeploymentLogger deploymentLogger,
								   @NotNull EnhancedTomcatRunConfiguration configuration) {
		super(process, commandLine, charset);
		this.deploymentLogger = deploymentLogger;
		this.configuration = configuration;
		this.processStartTime = System.currentTimeMillis();

		// Add process listener for enhanced logging
		addProcessListener(new DevTomcatProcessListener());

		System.out.println("DevTomcat: DevTomcatProcessHandler created successfully");
	}

	/**
	 * Inner class that implements ProcessListener for enhanced logging
	 */
	private class DevTomcatProcessListener implements ProcessListener {

		@Override
		public void startNotified(@NotNull ProcessEvent event) {
			deploymentLogger.logServerInfo("Tomcat process started");
			deploymentLogger.logDeploymentStart(getArtifactName());
			System.out.println("DevTomcat: Process start notification received");
		}

		@Override
		public void processTerminated(@NotNull ProcessEvent event) {
			int exitCode = event.getExitCode();
			long duration = System.currentTimeMillis() - processStartTime;

			if (exitCode == 0) {
				deploymentLogger.logServerInfo("Tomcat process terminated successfully after " + duration + " ms");
				System.out.println("DevTomcat: Process terminated successfully with exit code 0");
			} else {
				deploymentLogger.logServerError("Tomcat process terminated with exit code: " + exitCode);
				System.err.println("DevTomcat: Process terminated with error exit code: " + exitCode);
			}
		}

		@Override
		public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
			deploymentLogger.logServerInfo("Tomcat process will terminate (willBeDestroyed=" + willBeDestroyed + ")");
		}

		@Override
		public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
			String text = event.getText();
			if (text != null && !text.trim().isEmpty()) {
				analyzeOutput(text, outputType);
			}
		}
	}

	/**
	 * Analyze Tomcat output and provide enhanced logging
	 */
	private void analyzeOutput(String text, Key outputType) {
		try {
			// Only analyze stdout and stderr
			if (outputType != ProcessOutputTypes.STDOUT && outputType != ProcessOutputTypes.STDERR) {
				return;
			}

			String trimmedText = text.trim();

			// Server startup detection
			if (SERVER_STARTUP_PATTERN.matcher(trimmedText).find()) {
				Matcher matcher = SERVER_STARTUP_PATTERN.matcher(trimmedText);
				if (matcher.find()) {
					try {
						long startupTime = Long.parseLong(matcher.group(1));
						deploymentLogger.logServerStartup(startupTime);

						// Log deployment success for the artifact
						String artifactName = getArtifactName();
						deploymentLogger.logDeploymentSuccess(artifactName, startupTime);
						System.out.println("DevTomcat: Server startup detected - " + startupTime + " ms");
					} catch (NumberFormatException e) {
						deploymentLogger.logServerInfo("Server startup completed");
					}
				}
			}

			// Spring Boot startup detection
			else if (SPRING_STARTUP_PATTERN.matcher(trimmedText).find()) {
				Matcher matcher = SPRING_STARTUP_PATTERN.matcher(trimmedText);
				if (matcher.find()) {
					try {
						double startupTime = Double.parseDouble(matcher.group(1));
						deploymentLogger.logServerInfo("Spring Boot application started in " + startupTime + " seconds");
						System.out.println("DevTomcat: Spring Boot startup detected - " + startupTime + " seconds");
					} catch (NumberFormatException e) {
						deploymentLogger.logServerInfo("Spring Boot application started");
					}
				}
			}

			// JMX detection
			else if (JMX_ENABLED_PATTERN.matcher(trimmedText).find()) {
				Matcher matcher = JMX_ENABLED_PATTERN.matcher(trimmedText);
				if (matcher.find()) {
					String port = matcher.group(1);
					deploymentLogger.logServerInfo("JMX enabled on port " + port);
					System.out.println("DevTomcat: JMX detected on port " + port);
				}
			}

			// Hot deployment detection
			else if (HOT_DEPLOY_PATTERN.matcher(trimmedText).find()) {
				deploymentLogger.logServerInfo("Hot deployment: Context reloaded");
				System.out.println("DevTomcat: Hot deployment detected");
			}

			// Memory monitoring
			else if (MEMORY_PATTERN.matcher(trimmedText).find()) {
				Matcher matcher = MEMORY_PATTERN.matcher(trimmedText);
				if (matcher.find()) {
					String memoryUsage = matcher.group(1);
					deploymentLogger.logServerInfo("Memory usage: " + memoryUsage + "M heap");
				}
			}

			// Deployment detection
			else if (DEPLOYMENT_PATTERN.matcher(trimmedText).find()) {
				deploymentLogger.logServerInfo("Processing deployment...");
				System.out.println("DevTomcat: Deployment process detected");
			}

			// Deployment success detection
			else if (DEPLOYMENT_SUCCESS_PATTERN.matcher(trimmedText).find()) {
				Matcher matcher = DEPLOYMENT_SUCCESS_PATTERN.matcher(trimmedText);
				if (matcher.find()) {
					try {
						long deployTime = Long.parseLong(matcher.group(1));
						String artifactName = getArtifactName();
						deploymentLogger.logDeploymentSuccess(artifactName, deployTime);
						System.out.println("DevTomcat: Deployment success detected - " + deployTime + " ms");
					} catch (NumberFormatException e) {
						deploymentLogger.logServerInfo("Deployment completed successfully");
					}
				}
			}

			// Error detection
			else if (ERROR_PATTERN.matcher(trimmedText).find()) {
				deploymentLogger.logServerError("Tomcat error detected: " + trimmedText);
				System.err.println("DevTomcat: Error detected in logs: " + trimmedText);
			}

			// Warning detection
			else if (WARNING_PATTERN.matcher(trimmedText).find()) {
				deploymentLogger.logServerWarning("Tomcat warning: " + trimmedText);
				System.out.println("DevTomcat: Warning detected in logs: " + trimmedText);
			}

		} catch (Exception e) {
			// Don't let log analysis break the process handling
			deploymentLogger.logServerWarning("Log analysis error: " + e.getMessage());
			System.err.println("DevTomcat: Error analyzing output: " + e.getMessage());
		}
	}

	/**
	 * Get artifact name from configuration
	 */
	private String getArtifactName() {
		try {
			if (configuration != null) {
				String contextPath = configuration.getContextPath();
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
	 * Get the deployment logger for external access
	 */
	public TomcatDeploymentLogger getDeploymentLogger() {
		return deploymentLogger;
	}

	/**
	 * Get process start time
	 */
	public long getProcessStartTime() {
		return processStartTime;
	}

	/**
	 * Log deployment start for external triggers
	 */
	public void logDeploymentStart() {
		if (configuration != null) {
			String artifactName = getArtifactName();
			deploymentLogger.logDeploymentStart(artifactName);
		}
	}

	/**
	 * Get configuration for external access
	 */
	public EnhancedTomcatRunConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Performance monitoring - memory usage check
	 */
	private long lastMemoryLog = 0;
	private static final long MEMORY_LOG_INTERVAL = 30000; // 30 seconds

	private void checkMemoryUsage() {
		long now = System.currentTimeMillis();
		if (now - lastMemoryLog > MEMORY_LOG_INTERVAL) {
			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory() / 1024 / 1024;
			long freeMemory = runtime.freeMemory() / 1024 / 1024;
			long usedMemory = totalMemory - freeMemory;

			deploymentLogger.logServerInfo("DevTomcat Memory: " + usedMemory + "M used, " + freeMemory + "M free");
			lastMemoryLog = now;
		}
	}
}