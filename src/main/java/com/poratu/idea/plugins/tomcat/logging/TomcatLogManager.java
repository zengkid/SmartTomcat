package com.poratu.idea.plugins.tomcat.logging;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Phase 1: Simple log manager for console output management
 * Manages console views and prepares for Phase 2 file monitoring
 */
public class TomcatLogManager {

	private final Project project;
	private final Map<String, ConsoleView> consoleViews = new ConcurrentHashMap<>();
	private ConsoleView mainConsoleView;

	public TomcatLogManager(@NotNull Project project) {
		this.project = project;
	}

	/**
	 * Set main console view for Phase 1 enhanced logging
	 */
	public void setMainConsoleView(@NotNull ConsoleView consoleView) {
		this.mainConsoleView = consoleView;
		consoleViews.put("main", consoleView);
	}

	/**
	 * Get main console view
	 */
	@Nullable
	public ConsoleView getMainConsoleView() {
		return mainConsoleView;
	}

	/**
	 * Register a named console view (Phase 2 preparation)
	 */
	public void addConsoleView(@NotNull String name, @NotNull ConsoleView consoleView) {
		consoleViews.put(name, consoleView);
	}

	/**
	 * Remove a console view
	 */
	public void removeConsoleView(@NotNull String name) {
		consoleViews.remove(name);
	}

	/**
	 * Get a console view by name
	 */
	@Nullable
	public ConsoleView getConsoleView(@NotNull String name) {
		return consoleViews.get(name);
	}

	/**
	 * Get all registered console view names
	 */
	public String[] getConsoleViewNames() {
		return consoleViews.keySet().toArray(new String[0]);
	}

	/**
	 * Clear all console views
	 */
	public void clearAll() {
		consoleViews.clear();
		mainConsoleView = null;
	}

	/**
	 * Get project instance
	 */
	@NotNull
	public Project getProject() {
		return project;
	}

	/**
	 * Phase 2 placeholder: Add log file monitoring (not implemented in Phase 1)
	 */
	public void addLogFile(@NotNull String alias, @NotNull String filePath) {
		// Phase 2: Will implement file watching here
		// For now, just log that this feature is coming
		System.out.println("Phase 2 Feature: Log file monitoring for " + alias + " at " + filePath);
	}

	/**
	 * Phase 2 placeholder: Remove log file monitoring
	 */
	public void removeLogFile(@NotNull String alias) {
		// Phase 2: Will implement file watcher removal here
		System.out.println("Phase 2 Feature: Removing log file monitoring for " + alias);
	}

	/**
	 * Phase 2 placeholder: Remove all log file monitoring
	 */
	public void removeAllLogFiles() {
		// Phase 2: Will implement cleanup here
		System.out.println("Phase 2 Feature: Removing all log file monitoring");
	}
}