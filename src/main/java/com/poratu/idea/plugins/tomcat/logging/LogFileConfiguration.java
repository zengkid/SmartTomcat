package com.poratu.idea.plugins.tomcat.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Phase 2: Configuration for individual log files
 * Represents a single log file that can be monitored and displayed in a separate tab
 */
public class LogFileConfiguration {

	private String alias;
	private String filePath;
	private boolean active;
	private String description;
	private boolean skipContent;
	private boolean showAllMessages;

	/**
	 * Default constructor
	 */
	public LogFileConfiguration() {
		this("", "", true, "");
	}

	/**
	 * Constructor with basic parameters
	 */
	public LogFileConfiguration(@NotNull String alias, @NotNull String filePath, boolean active, @Nullable String description) {
		this.alias = alias;
		this.filePath = filePath;
		this.active = active;
		this.description = description != null ? description : "";
		this.skipContent = false;
		this.showAllMessages = true;
	}

	/**
	 * Full constructor
	 */
	public LogFileConfiguration(@NotNull String alias, @NotNull String filePath, boolean active,
								@Nullable String description, boolean skipContent, boolean showAllMessages) {
		this.alias = alias;
		this.filePath = filePath;
		this.active = active;
		this.description = description != null ? description : "";
		this.skipContent = skipContent;
		this.showAllMessages = showAllMessages;
	}

	/**
	 * Copy constructor
	 */
	public LogFileConfiguration(@NotNull LogFileConfiguration other) {
		this.alias = other.alias;
		this.filePath = other.filePath;
		this.active = other.active;
		this.description = other.description;
		this.skipContent = other.skipContent;
		this.showAllMessages = other.showAllMessages;
	}

	// Getters and Setters

	@NotNull
	public String getAlias() {
		return alias;
	}

	public void setAlias(@NotNull String alias) {
		this.alias = alias;
	}

	@NotNull
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(@NotNull String filePath) {
		this.filePath = filePath;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@NotNull
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description != null ? description : "";
	}

	public boolean isSkipContent() {
		return skipContent;
	}

	public void setSkipContent(boolean skipContent) {
		this.skipContent = skipContent;
	}

	public boolean isShowAllMessages() {
		return showAllMessages;
	}

	public void setShowAllMessages(boolean showAllMessages) {
		this.showAllMessages = showAllMessages;
	}

	/**
	 * Resolve variables in file path
	 * Supports: $CATALINA_BASE, $CATALINA_HOME, $DATE
	 */
	@NotNull
	public String getResolvedFilePath(@Nullable String catalinaBase, @Nullable String catalinaHome) {
		String resolved = filePath;

		// Replace variables
		if (catalinaBase != null) {
			resolved = resolved.replace("$CATALINA_BASE", catalinaBase);
		}
		if (catalinaHome != null) {
			resolved = resolved.replace("$CATALINA_HOME", catalinaHome);
		}

		// Replace date
		String currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		resolved = resolved.replace("$DATE", currentDate);

		return resolved;
	}

	/**
	 * Check if this configuration is valid
	 */
	public boolean isValid() {
		return alias != null && !alias.trim().isEmpty() &&
				filePath != null && !filePath.trim().isEmpty();
	}

	/**
	 * Get display name for UI
	 */
	@NotNull
	public String getDisplayName() {
		if (description != null && !description.trim().isEmpty()) {
			return alias + " (" + description + ")";
		}
		return alias;
	}

	/**
	 * Check if this is a default Tomcat log file
	 */
	public boolean isDefaultTomcatLog() {
		return "Catalina".equals(alias) || "Localhost".equals(alias) ||
				"Manager".equals(alias) || "Host-Manager".equals(alias);
	}

	/**
	 * Create a default Catalina log configuration
	 */
	public static LogFileConfiguration createCatalinaLog() {
		return new LogFileConfiguration(
				"Catalina",
				"$CATALINA_BASE/logs/catalina.out",
				true,
				"Main Tomcat server log"
		);
	}

	/**
	 * Create a default Localhost log configuration
	 */
	public static LogFileConfiguration createLocalhostLog() {
		return new LogFileConfiguration(
				"Localhost",
				"$CATALINA_BASE/logs/localhost.$DATE.log",
				true,
				"Localhost application log"
		);
	}

	/**
	 * Create a default Manager log configuration
	 */
	public static LogFileConfiguration createManagerLog() {
		return new LogFileConfiguration(
				"Manager",
				"$CATALINA_BASE/logs/manager.$DATE.log",
				false,
				"Tomcat Manager application log"
		);
	}

	/**
	 * Create a default Host-Manager log configuration
	 */
	public static LogFileConfiguration createHostManagerLog() {
		return new LogFileConfiguration(
				"Host-Manager",
				"$CATALINA_BASE/logs/host-manager.$DATE.log",
				false,
				"Tomcat Host Manager log"
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LogFileConfiguration that = (LogFileConfiguration) o;
		return active == that.active &&
				skipContent == that.skipContent &&
				showAllMessages == that.showAllMessages &&
				Objects.equals(alias, that.alias) &&
				Objects.equals(filePath, that.filePath) &&
				Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alias, filePath, active, description, skipContent, showAllMessages);
	}

	@Override
	public String toString() {
		return "LogFileConfiguration{" +
				"alias='" + alias + '\'' +
				", filePath='" + filePath + '\'' +
				", active=" + active +
				", description='" + description + '\'' +
				", skipContent=" + skipContent +
				", showAllMessages=" + showAllMessages +
				'}';
	}
}
