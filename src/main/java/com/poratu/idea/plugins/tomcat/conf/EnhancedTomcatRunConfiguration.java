package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.poratu.idea.plugins.tomcat.logging.LogFileConfiguration;
import com.poratu.idea.plugins.tomcat.ui.EnhancedTomcatConfigurationEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2: Enhanced Tomcat run configuration with Ultimate-style 5-tab interface
 * Extends the basic TomcatRunConfiguration with advanced features:
 * - JMX integration
 * - Multiple log file monitoring
 * - Environment variables
 * - Connection settings
 * - Hot deployment
 */
public class EnhancedTomcatRunConfiguration extends TomcatRunConfiguration {

    // Phase 2: Enhanced configuration properties

    // JMX Configuration
    private boolean jmxEnabled = false;
    private String jmxHost = "localhost";
    private int jmxPort = 1099;
    private boolean jmxSslEnabled = false;
    private boolean jmxAuthEnabled = false;
    private String jmxUsername = "";
    private String jmxPassword = "";

    // Log File Configuration
    private List<LogFileConfiguration> logFileConfigurations = new ArrayList<>();
    private boolean loggingEnabled = true;
    private boolean skipContent = false;
    private boolean showAllMessages = true;

    // Environment Variables
    private Map<String, String> environmentVariables = new HashMap<>();

    // Connection Settings
    private int connectionTimeout = 30000;
    private int readTimeout = 60000;
    private boolean remoteDebuggingEnabled = false;
    private int debugPort = 5005;

    // Hot Deployment Settings
    private boolean hotDeploymentEnabled = false;
    private boolean updateClassesAndResources = true;
    private boolean updateTriggerFiles = false;

    // Code Coverage Settings
    private boolean coverageEnabled = false;
    private boolean trackPerTest = false;

    public EnhancedTomcatRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        initializeDefaultConfiguration();
    }

    /**
     * Initialize default configuration values
     */
    private void initializeDefaultConfiguration() {
        // Initialize default log files
        logFileConfigurations.add(LogFileConfiguration.createCatalinaLog());
        logFileConfigurations.add(LogFileConfiguration.createLocalhostLog());

        // Initialize default environment variables
        environmentVariables.put("JAVA_OPTS", "-Xmx512m -Xms256m");
        environmentVariables.put("CATALINA_OPTS", "-Dfile.encoding=UTF-8");
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new EnhancedTomcatConfigurationEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        // Use enhanced command line state for Phase 2 features
        return new EnhancedTomcatCommandLineState(environment, this);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);

        // Read Phase 2 specific configuration
        readJmxConfiguration(element);
        readLogConfiguration(element);
        readEnvironmentConfiguration(element);
        readConnectionConfiguration(element);
        readHotDeploymentConfiguration(element);
        readCoverageConfiguration(element);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);

        // Write Phase 2 specific configuration
        writeJmxConfiguration(element);
        writeLogConfiguration(element);
        writeEnvironmentConfiguration(element);
        writeConnectionConfiguration(element);
        writeHotDeploymentConfiguration(element);
        writeCoverageConfiguration(element);
    }

    /**
     * Read JMX configuration from XML
     */
    private void readJmxConfiguration(@NotNull Element element) {
        Element jmxElement = element.getChild("jmx");
        if (jmxElement != null) {
            jmxEnabled = Boolean.parseBoolean(jmxElement.getAttributeValue("enabled", "false"));
            jmxHost = jmxElement.getAttributeValue("host", "localhost");
            jmxPort = Integer.parseInt(jmxElement.getAttributeValue("port", "1099"));
            jmxSslEnabled = Boolean.parseBoolean(jmxElement.getAttributeValue("ssl", "false"));
            jmxAuthEnabled = Boolean.parseBoolean(jmxElement.getAttributeValue("auth", "false"));
            jmxUsername = jmxElement.getAttributeValue("username", "");
            jmxPassword = jmxElement.getAttributeValue("password", "");
        }
    }

    /**
     * Write JMX configuration to XML
     */
    private void writeJmxConfiguration(@NotNull Element element) {
        Element jmxElement = new Element("jmx");
        jmxElement.setAttribute("enabled", String.valueOf(jmxEnabled));
        jmxElement.setAttribute("host", jmxHost);
        jmxElement.setAttribute("port", String.valueOf(jmxPort));
        jmxElement.setAttribute("ssl", String.valueOf(jmxSslEnabled));
        jmxElement.setAttribute("auth", String.valueOf(jmxAuthEnabled));
        jmxElement.setAttribute("username", jmxUsername);
        jmxElement.setAttribute("password", jmxPassword);
        element.addContent(jmxElement);
    }

    /**
     * Read log configuration from XML
     */
    private void readLogConfiguration(@NotNull Element element) {
        Element logsElement = element.getChild("logs");
        if (logsElement != null) {
            loggingEnabled = Boolean.parseBoolean(logsElement.getAttributeValue("enabled", "true"));
            skipContent = Boolean.parseBoolean(logsElement.getAttributeValue("skipContent", "false"));
            showAllMessages = Boolean.parseBoolean(logsElement.getAttributeValue("showAll", "true"));

            logFileConfigurations.clear();
            for (Element logFileElement : logsElement.getChildren("logFile")) {
                LogFileConfiguration config = new LogFileConfiguration(
                        logFileElement.getAttributeValue("alias", ""),
                        logFileElement.getAttributeValue("path", ""),
                        Boolean.parseBoolean(logFileElement.getAttributeValue("active", "true")),
                        logFileElement.getAttributeValue("description", ""),
                        Boolean.parseBoolean(logFileElement.getAttributeValue("skipContent", "false")),
                        Boolean.parseBoolean(logFileElement.getAttributeValue("showAll", "true"))
                );
                logFileConfigurations.add(config);
            }
        }
    }

    /**
     * Write log configuration to XML
     */
    private void writeLogConfiguration(@NotNull Element element) {
        Element logsElement = new Element("logs");
        logsElement.setAttribute("enabled", String.valueOf(loggingEnabled));
        logsElement.setAttribute("skipContent", String.valueOf(skipContent));
        logsElement.setAttribute("showAll", String.valueOf(showAllMessages));

        for (LogFileConfiguration config : logFileConfigurations) {
            Element logFileElement = new Element("logFile");
            logFileElement.setAttribute("alias", config.getAlias());
            logFileElement.setAttribute("path", config.getFilePath());
            logFileElement.setAttribute("active", String.valueOf(config.isActive()));
            logFileElement.setAttribute("description", config.getDescription());
            logFileElement.setAttribute("skipContent", String.valueOf(config.isSkipContent()));
            logFileElement.setAttribute("showAll", String.valueOf(config.isShowAllMessages()));
            logsElement.addContent(logFileElement);
        }

        element.addContent(logsElement);
    }

    /**
     * Read environment configuration from XML
     */
    private void readEnvironmentConfiguration(@NotNull Element element) {
        Element envElement = element.getChild("environment");
        if (envElement != null) {
            environmentVariables.clear();
            for (Element varElement : envElement.getChildren("variable")) {
                String name = varElement.getAttributeValue("name");
                String value = varElement.getAttributeValue("value");
                if (name != null && value != null) {
                    environmentVariables.put(name, value);
                }
            }
        }
    }

    /**
     * Write environment configuration to XML
     */
    private void writeEnvironmentConfiguration(@NotNull Element element) {
        Element envElement = new Element("environment");
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            Element varElement = new Element("variable");
            varElement.setAttribute("name", entry.getKey());
            varElement.setAttribute("value", entry.getValue());
            envElement.addContent(varElement);
        }
        element.addContent(envElement);
    }

    /**
     * Read connection configuration from XML
     */
    private void readConnectionConfiguration(@NotNull Element element) {
        Element connectionElement = element.getChild("connection");
        if (connectionElement != null) {
            connectionTimeout = Integer.parseInt(connectionElement.getAttributeValue("timeout", "30000"));
            readTimeout = Integer.parseInt(connectionElement.getAttributeValue("readTimeout", "60000"));
            remoteDebuggingEnabled = Boolean.parseBoolean(connectionElement.getAttributeValue("debugEnabled", "false"));
            debugPort = Integer.parseInt(connectionElement.getAttributeValue("debugPort", "5005"));
            deploymentTimeout = Integer.parseInt(connectionElement.getAttributeValue("deploymentTimeout", "30"));

        }
    }

    /**
     * Write connection configuration to XML
     */
    private void writeConnectionConfiguration(@NotNull Element element) {
        Element connectionElement = new Element("connection");
        connectionElement.setAttribute("timeout", String.valueOf(connectionTimeout));
        connectionElement.setAttribute("readTimeout", String.valueOf(readTimeout));
        connectionElement.setAttribute("debugEnabled", String.valueOf(remoteDebuggingEnabled));
        connectionElement.setAttribute("debugPort", String.valueOf(debugPort));
        connectionElement.setAttribute("deploymentTimeout", String.valueOf(deploymentTimeout));
        element.addContent(connectionElement);
    }

    /**
     * Read hot deployment configuration from XML
     */
    private void readHotDeploymentConfiguration(@NotNull Element element) {
        Element hotDeployElement = element.getChild("hotDeploy");
        if (hotDeployElement != null) {
            hotDeploymentEnabled = Boolean.parseBoolean(hotDeployElement.getAttributeValue("enabled", "false"));
            updateClassesAndResources = Boolean.parseBoolean(hotDeployElement.getAttributeValue("updateClasses", "true"));
            updateTriggerFiles = Boolean.parseBoolean(hotDeployElement.getAttributeValue("updateTriggers", "false"));
            enableAccessLog = Boolean.parseBoolean(hotDeployElement.getAttributeValue("enableAccessLog", "true"));
            accessLogPattern = hotDeployElement.getAttributeValue("accessLogPattern", "combined");
        }
    }

    /**
     * Write hot deployment configuration to XML
     */
    private void writeHotDeploymentConfiguration(@NotNull Element element) {
        Element hotDeployElement = new Element("hotDeploy");
        hotDeployElement.setAttribute("enabled", String.valueOf(hotDeploymentEnabled));
        hotDeployElement.setAttribute("updateClasses", String.valueOf(updateClassesAndResources));
        hotDeployElement.setAttribute("updateTriggers", String.valueOf(updateTriggerFiles));
        hotDeployElement.setAttribute("enableAccessLog", String.valueOf(enableAccessLog));
        hotDeployElement.setAttribute("accessLogPattern", accessLogPattern);
        element.addContent(hotDeployElement);
    }

    /**
     * Read code coverage configuration from XML
     */
    private void readCoverageConfiguration(@NotNull Element element) {
        Element coverageElement = element.getChild("coverage");
        if (coverageElement != null) {
            coverageEnabled = Boolean.parseBoolean(coverageElement.getAttributeValue("enabled", "false"));
            trackPerTest = Boolean.parseBoolean(coverageElement.getAttributeValue("perTest", "false"));
        }
    }

    /**
     * Write code coverage configuration to XML
     */
    private void writeCoverageConfiguration(@NotNull Element element) {
        Element coverageElement = new Element("coverage");
        coverageElement.setAttribute("enabled", String.valueOf(coverageEnabled));
        coverageElement.setAttribute("perTest", String.valueOf(trackPerTest));
        element.addContent(coverageElement);
    }

    // Getters and Setters for Phase 2 configuration

    // JMX Configuration
    public boolean isJmxEnabled() { return jmxEnabled; }
    public void setJmxEnabled(boolean jmxEnabled) { this.jmxEnabled = jmxEnabled; }

    public String getJmxHost() { return jmxHost; }
    public void setJmxHost(String jmxHost) { this.jmxHost = jmxHost; }

    public int getJmxPort() { return jmxPort; }
    public void setJmxPort(int jmxPort) { this.jmxPort = jmxPort; }

    public boolean isJmxSslEnabled() { return jmxSslEnabled; }
    public void setJmxSslEnabled(boolean jmxSslEnabled) { this.jmxSslEnabled = jmxSslEnabled; }

    public boolean isJmxAuthEnabled() { return jmxAuthEnabled; }
    public void setJmxAuthEnabled(boolean jmxAuthEnabled) { this.jmxAuthEnabled = jmxAuthEnabled; }

    public String getJmxUsername() { return jmxUsername; }
    public void setJmxUsername(String jmxUsername) { this.jmxUsername = jmxUsername; }

    public String getJmxPassword() { return jmxPassword; }
    public void setJmxPassword(String jmxPassword) { this.jmxPassword = jmxPassword; }

    // Log Configuration
    public List<LogFileConfiguration> getLogFileConfigurations() { return new ArrayList<>(logFileConfigurations); }
    public void setLogFileConfigurations(List<LogFileConfiguration> configurations) {
        this.logFileConfigurations = new ArrayList<>(configurations);
    }

    public boolean isLoggingEnabled() { return loggingEnabled; }
    public void setLoggingEnabled(boolean loggingEnabled) { this.loggingEnabled = loggingEnabled; }

    public boolean isSkipContent() { return skipContent; }
    public void setSkipContent(boolean skipContent) { this.skipContent = skipContent; }

    public boolean isShowAllMessages() { return showAllMessages; }
    public void setShowAllMessages(boolean showAllMessages) { this.showAllMessages = showAllMessages; }

    // Environment Variables
    public Map<String, String> getEnvironmentVariables() { return new HashMap<>(environmentVariables); }
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = new HashMap<>(environmentVariables);
    }

    // Connection Settings
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }

    public boolean isRemoteDebuggingEnabled() { return remoteDebuggingEnabled; }
    public void setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled) { this.remoteDebuggingEnabled = remoteDebuggingEnabled; }

    public int getDebugPort() { return debugPort; }
    public void setDebugPort(int debugPort) { this.debugPort = debugPort; }

    // Hot Deployment Settings
    public boolean isHotDeploymentEnabled() { return hotDeploymentEnabled; }
    public void setHotDeploymentEnabled(boolean hotDeploymentEnabled) { this.hotDeploymentEnabled = hotDeploymentEnabled; }

    public boolean isUpdateClassesAndResources() { return updateClassesAndResources; }
    public void setUpdateClassesAndResources(boolean updateClassesAndResources) { this.updateClassesAndResources = updateClassesAndResources; }

    public boolean isUpdateTriggerFiles() { return updateTriggerFiles; }
    public void setUpdateTriggerFiles(boolean updateTriggerFiles) { this.updateTriggerFiles = updateTriggerFiles; }

    /**
     * Get JMX VM options for Tomcat startup
     */
    public String getJmxVmOptions() {
        if (!jmxEnabled) {
            return "";
        }

        StringBuilder options = new StringBuilder();
        options.append("-Dcom.sun.management.jmxremote");
        options.append(" -Dcom.sun.management.jmxremote.port=").append(jmxPort);
        options.append(" -Dcom.sun.management.jmxremote.ssl=").append(jmxSslEnabled);
        options.append(" -Dcom.sun.management.jmxremote.authenticate=").append(jmxAuthEnabled);

        if (!jmxAuthEnabled) {
            options.append(" -Dcom.sun.management.jmxremote.local.only=false");
        }

        return options.toString();
    }

    /**
     * Get remote debugging VM options
     */
    public String getDebugVmOptions() {
        if (!remoteDebuggingEnabled) {
            return "";
        }

        return String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%d", debugPort);
    }

    @Override
    public RunConfiguration clone() {
        EnhancedTomcatRunConfiguration clone = (EnhancedTomcatRunConfiguration) super.clone();

        // Deep clone Phase 2 specific configurations
        clone.logFileConfigurations = new ArrayList<>();
        for (LogFileConfiguration config : this.logFileConfigurations) {
            clone.logFileConfigurations.add(new LogFileConfiguration(config));
        }

        clone.environmentVariables = new HashMap<>(this.environmentVariables);

        return clone;
    }

    // Add these exact missing methods to your EnhancedTomcatRunConfiguration class:

    // 1. Missing field and methods for deploymentTimeout (used in DeploymentConfigurationTab)
    private int deploymentTimeout = 30;

    public int getDeploymentTimeout() {
        return deploymentTimeout;
    }

    public void setDeploymentTimeout(int deploymentTimeout) {
        this.deploymentTimeout = deploymentTimeout;
    }

    // 2. Missing fields and methods for access log (used in DeploymentConfigurationTab)
    private boolean enableAccessLog = true;
    private String accessLogPattern = "combined";

    public boolean isEnableAccessLog() {
        return enableAccessLog;
    }

    public void setEnableAccessLog(boolean enableAccessLog) {
        this.enableAccessLog = enableAccessLog;
    }

    public String getAccessLogPattern() {
        return accessLogPattern;
    }

    public void setAccessLogPattern(String accessLogPattern) {
        this.accessLogPattern = accessLogPattern;
    }

    // Code Coverage
    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    public void setCoverageEnabled(boolean coverageEnabled) {
        this.coverageEnabled = coverageEnabled;
    }

    public boolean isTrackPerTest() {
        return trackPerTest;
    }

    public void setTrackPerTest(boolean trackPerTest) {
        this.trackPerTest = trackPerTest;
    }


}