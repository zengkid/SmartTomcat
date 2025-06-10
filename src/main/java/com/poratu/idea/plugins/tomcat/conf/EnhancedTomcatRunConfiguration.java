package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.poratu.idea.plugins.tomcat.logging.LogFileConfiguration;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.ui.EnhancedTomcatConfigurationEditor;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enhanced Tomcat Run Configuration - Phase 2 with Ultimate-like features
 * This is the main configuration class that replaces the basic TomcatRunConfiguration
 */
public class EnhancedTomcatRunConfiguration extends LocatableConfigurationBase<LocatableRunConfigurationOptions>
        implements RunProfileWithCompileBeforeLaunchOption {

    private static final List<TomcatLogFile> tomcatLogFiles = Arrays.asList(
            new TomcatLogFile(TomcatLogFile.TOMCAT_LOCALHOST_LOG_ID, "localhost", true),
            new TomcatLogFile(TomcatLogFile.TOMCAT_ACCESS_LOG_ID, "localhost_access_log", true),
            new TomcatLogFile(TomcatLogFile.TOMCAT_CATALINA_LOG_ID, "catalina"),
            new TomcatLogFile(TomcatLogFile.TOMCAT_MANAGER_LOG_ID, "manager"),
            new TomcatLogFile(TomcatLogFile.TOMCAT_HOST_MANAGER_LOG_ID, "host-manager")
    );

    private static List<PredefinedLogFile> createPredefinedLogFiles() {
        return tomcatLogFiles.stream()
                .map(TomcatLogFile::createPredefinedLogFile)
                .collect(Collectors.toList());
    }

    // Enhanced configuration options
    private EnhancedTomcatRunConfigurationOptions enhancedOptions = new EnhancedTomcatRunConfigurationOptions();
    private RunConfigurationModule configurationModule;
    private boolean coverageEnabled = false;
    private boolean trackPerTest = false;

    public boolean isCoverageEnabled() { return coverageEnabled; }
    public void setCoverageEnabled(boolean coverageEnabled) { this.coverageEnabled = coverageEnabled; }

    public boolean isTrackPerTest() { return trackPerTest; }
    public void setTrackPerTest(boolean trackPerTest) { this.trackPerTest = trackPerTest; }

    public EnhancedTomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        configurationModule = new RunConfigurationModule(project);

        System.out.println("DevTomcat: EnhancedTomcatRunConfiguration constructor called");

        try {
            TomcatServerManagerState applicationService = ApplicationManager.getApplication().getService(TomcatServerManagerState.class);
            List<TomcatInfo> tomcatInfos = applicationService.getTomcatInfos();
            System.out.println("DevTomcat: Found " + tomcatInfos.size() + " Tomcat servers");

            if (!tomcatInfos.isEmpty()) {
                enhancedOptions.setTomcatInfo(tomcatInfos.get(0));
                System.out.println("DevTomcat: Using Tomcat server: " + tomcatInfos.get(0).getName());
            } else {
                System.out.println("DevTomcat: WARNING - No Tomcat servers configured!");
            }

            // Initialize enhanced features
            initializeEnhancedFeatures();
            addPredefinedTomcatLogFiles();
            System.out.println("DevTomcat: EnhancedTomcatRunConfiguration created successfully");

        } catch (Exception e) {
            System.err.println("DevTomcat: Error in constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize enhanced Phase 2 features
     */
    private void initializeEnhancedFeatures() {
        // Initialize default log files
        enhancedOptions.getLogFileConfigurations().add(LogFileConfiguration.createCatalinaLog());
        enhancedOptions.getLogFileConfigurations().add(LogFileConfiguration.createLocalhostLog());

        // Initialize default environment variables
        enhancedOptions.getEnvironmentVariables().put("JAVA_OPTS", "-Xmx512m -Xms256m");
        enhancedOptions.getEnvironmentVariables().put("CATALINA_OPTS", "-Dfile.encoding=UTF-8");
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        System.out.println("DevTomcat: Using Enhanced Ultimate-style 5-tab interface");
        return new EnhancedTomcatConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        System.out.println("DevTomcat: Checking enhanced configuration...");

        if (getTomcatInfo() == null) {
            System.err.println("DevTomcat: No Tomcat server selected");
            throw new RuntimeConfigurationError("Tomcat server is not selected");
        }

        if (StringUtil.isEmpty(getDocBase())) {
            System.err.println("DevTomcat: No deployment directory");
            throw new RuntimeConfigurationError("Deployment directory cannot be empty");
        }

        if (StringUtil.isEmpty(getContextPath())) {
            System.err.println("DevTomcat: No context path");
            throw new RuntimeConfigurationError("Context path cannot be empty");
        }

        if (getModule() == null) {
            System.err.println("DevTomcat: No module selected");
            throw new RuntimeConfigurationError("Module is not selected");
        }

        if (getPort() == null) {
            System.err.println("DevTomcat: Port not configured");
            throw new RuntimeConfigurationError("Port cannot be empty");
        }

        System.out.println("DevTomcat: Enhanced configuration check passed!");
    }

    @Override
    public void onNewConfigurationCreated() {
        super.onNewConfigurationCreated();

        try {
            Project project = getProject();
            List<VirtualFile> webRoots = PluginUtils.findWebRoots(project);

            if (!webRoots.isEmpty()) {
                VirtualFile webRoot = webRoots.get(0);
                enhancedOptions.setDocBase(webRoot.getPath());
                Module module = PluginUtils.findContainingModule(webRoot.getPath(), project);

                if (module == null) {
                    module = PluginUtils.guessModule(project);
                }

                if (module != null) {
                    enhancedOptions.setContextPath("/" + PluginUtils.extractContextPath(module));
                }

                configurationModule.setModule(module);
            }
        } catch (Exception e) {
            System.err.println("DevTomcat: Error in onNewConfigurationCreated: " + e.getMessage());
        }
    }

    @Override
    public Module @NotNull [] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        System.out.println("DevTomcat: getState() called - using EnhancedTomcatCommandLineState");
        return new EnhancedTomcatCommandLineState(executionEnvironment, this);
    }


    @Override
    public @Nullable LogFileOptions getOptionsForPredefinedLogFile(PredefinedLogFile file) {
        for (TomcatLogFile logFile : tomcatLogFiles) {
            if (logFile.getId().equals(file.getId())) {
                return logFile.createLogFileOptions(file, PluginUtils.getTomcatLogsDirPath(this));
            }
        }
        return super.getOptionsForPredefinedLogFile(file);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(element, enhancedOptions);
        configurationModule.readExternal(element);

        if (getAllLogFiles().isEmpty()) {
            addPredefinedTomcatLogFiles();
        }

        if (configurationModule.getModule() == null) {
            configurationModule.setModule(PluginUtils.findContainingModule(enhancedOptions.getDocBase(), getProject()));
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeObjectInto(enhancedOptions, element);
        if (configurationModule.getModule() != null) {
            configurationModule.writeExternal(element);
        }
    }

    private void addPredefinedTomcatLogFiles() {
        createPredefinedLogFiles().forEach(this::addPredefinedLogFile);
    }

    @Override
    public RunConfiguration clone() {
        EnhancedTomcatRunConfiguration clone = (EnhancedTomcatRunConfiguration) super.clone();
        clone.configurationModule = new RunConfigurationModule(getProject());
        clone.configurationModule.setModule(configurationModule.getModule());
        clone.enhancedOptions = XmlSerializerUtil.createCopy(enhancedOptions);
        return clone;
    }

    // Basic Tomcat configuration methods (inherited from original)
    @Nullable
    public Module getModule() {
        return this.configurationModule.getModule();
    }

    public void setModule(Module module) {
        this.configurationModule.setModule(module);
    }

    public TomcatInfo getTomcatInfo() {
        return enhancedOptions.getTomcatInfo();
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        enhancedOptions.setTomcatInfo(tomcatInfo);
    }

    public String getCatalinaBase() {
        return enhancedOptions.getCatalinaBase();
    }

    public void setCatalinaBase(String catalinaBase) {
        enhancedOptions.setCatalinaBase(catalinaBase);
    }

    public String getDocBase() {
        return enhancedOptions.getDocBase();
    }

    public void setDocBase(String docBase) {
        enhancedOptions.setDocBase(docBase);
    }

    public String getContextPath() {
        return enhancedOptions.getContextPath();
    }

    public void setContextPath(String contextPath) {
        enhancedOptions.setContextPath(contextPath);
    }

    public Integer getPort() {
        return enhancedOptions.getPort();
    }

    public void setPort(Integer port) {
        enhancedOptions.setPort(port);
    }

    public Integer getSslPort() {
        return enhancedOptions.getSslPort();
    }

    public void setSslPort(Integer sslPort) {
        enhancedOptions.setSslPort(sslPort);
    }

    public Integer getAdminPort() {
        return enhancedOptions.getAdminPort();
    }

    public void setAdminPort(Integer adminPort) {
        enhancedOptions.setAdminPort(adminPort);
    }

    public String getVmOptions() {
        return enhancedOptions.getVmOptions();
    }

    public void setVmOptions(String vmOptions) {
        enhancedOptions.setVmOptions(vmOptions);
    }

    public Map<String, String> getEnvOptions() {
        return enhancedOptions.getEnvOptions();
    }

    public void setEnvOptions(Map<String, String> envOptions) {
        enhancedOptions.setEnvOptions(envOptions);
    }

    public Boolean isPassParentEnvs() {
        return enhancedOptions.isPassParentEnvs();
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvs) {
        enhancedOptions.setPassParentEnvs(passParentEnvs);
    }

    public String getExtraClassPath() {
        return enhancedOptions.getExtraClassPath();
    }

    public void setExtraClassPath(String extraClassPath) {
        enhancedOptions.setExtraClassPath(extraClassPath);
    }

    // Enhanced Phase 2 configuration methods
    public boolean isJmxEnabled() {
        return enhancedOptions.isJmxEnabled();
    }

    public void setJmxEnabled(boolean jmxEnabled) {
        enhancedOptions.setJmxEnabled(jmxEnabled);
    }

    public String getJmxHost() {
        return enhancedOptions.getJmxHost();
    }

    public void setJmxHost(String jmxHost) {
        enhancedOptions.setJmxHost(jmxHost);
    }

    public int getJmxPort() {
        return enhancedOptions.getJmxPort();
    }

    public void setJmxPort(int jmxPort) {
        enhancedOptions.setJmxPort(jmxPort);
    }

    public List<LogFileConfiguration> getLogFileConfigurations() {
        return enhancedOptions.getLogFileConfigurations();
    }

    public void setLogFileConfigurations(List<LogFileConfiguration> configurations) {
        enhancedOptions.setLogFileConfigurations(configurations);
    }

    public Map<String, String> getEnvironmentVariables() {
        return enhancedOptions.getEnvironmentVariables();
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        enhancedOptions.setEnvironmentVariables(environmentVariables);
    }

    public boolean isHotDeploymentEnabled() {
        return enhancedOptions.isHotDeploymentEnabled();
    }

    public void setHotDeploymentEnabled(boolean hotDeploymentEnabled) {
        enhancedOptions.setHotDeploymentEnabled(hotDeploymentEnabled);
    }

    public boolean isUpdateClassesAndResources() {
        return enhancedOptions.isUpdateClassesAndResources();
    }

    public void setUpdateClassesAndResources(boolean updateClassesAndResources) {
        enhancedOptions.setUpdateClassesAndResources(updateClassesAndResources);
    }

    public int getDeploymentTimeout() {
        return enhancedOptions.getDeploymentTimeout();
    }

    public void setDeploymentTimeout(int deploymentTimeout) {
        enhancedOptions.setDeploymentTimeout(deploymentTimeout);
    }

    public boolean isEnableAccessLog() {
        return enhancedOptions.isEnableAccessLog();
    }

    public void setEnableAccessLog(boolean enableAccessLog) {
        enhancedOptions.setEnableAccessLog(enableAccessLog);
    }

    public String getAccessLogPattern() {
        return enhancedOptions.getAccessLogPattern();
    }

    public void setAccessLogPattern(String accessLogPattern) {
        enhancedOptions.setAccessLogPattern(accessLogPattern);
    }

    /**
     * Enhanced configuration options class - combines all Phase 2 features
     */
    private static class EnhancedTomcatRunConfigurationOptions implements Serializable {
        // Basic Tomcat options
        private TomcatInfo tomcatInfo;
        private String catalinaBase;
        private String docBase;
        private String contextPath;
        private Integer port = 8080;
        private Integer sslPort;
        private Integer adminPort = 8005;
        private String vmOptions;
        private Map<String, String> envOptions = new HashMap<>();
        private Boolean passParentEnvs = true;
        private String extraClassPath;

        // Enhanced Phase 2 options
        private boolean jmxEnabled = false;
        private String jmxHost = "localhost";
        private int jmxPort = 1099;
        private List<LogFileConfiguration> logFileConfigurations = new ArrayList<>();
        private Map<String, String> environmentVariables = new HashMap<>();
        private boolean hotDeploymentEnabled = false;
        private boolean updateClassesAndResources = true;
        private int deploymentTimeout = 30;
        private boolean enableAccessLog = true;
        private String accessLogPattern = "combined";

        // Basic getters/setters
        public TomcatInfo getTomcatInfo() { return tomcatInfo; }
        public void setTomcatInfo(TomcatInfo tomcatInfo) { this.tomcatInfo = tomcatInfo; }

        public String getCatalinaBase() { return catalinaBase; }
        public void setCatalinaBase(String catalinaBase) { this.catalinaBase = catalinaBase; }

        public String getDocBase() { return docBase; }
        public void setDocBase(String docBase) { this.docBase = docBase; }

        public String getContextPath() { return contextPath; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }

        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }

        public Integer getSslPort() { return sslPort; }
        public void setSslPort(Integer sslPort) { this.sslPort = sslPort; }

        public Integer getAdminPort() { return adminPort; }
        public void setAdminPort(Integer adminPort) { this.adminPort = adminPort; }

        public String getVmOptions() { return vmOptions; }
        public void setVmOptions(String vmOptions) { this.vmOptions = vmOptions; }

        public Map<String, String> getEnvOptions() { return envOptions; }
        public void setEnvOptions(Map<String, String> envOptions) { this.envOptions = envOptions; }

        public Boolean isPassParentEnvs() { return passParentEnvs; }
        public void setPassParentEnvs(Boolean passParentEnvs) { this.passParentEnvs = passParentEnvs; }

        public String getExtraClassPath() { return extraClassPath; }
        public void setExtraClassPath(String extraClassPath) { this.extraClassPath = extraClassPath; }

        // Enhanced getters/setters
        public boolean isJmxEnabled() { return jmxEnabled; }
        public void setJmxEnabled(boolean jmxEnabled) { this.jmxEnabled = jmxEnabled; }

        public String getJmxHost() { return jmxHost; }
        public void setJmxHost(String jmxHost) { this.jmxHost = jmxHost; }

        public int getJmxPort() { return jmxPort; }
        public void setJmxPort(int jmxPort) { this.jmxPort = jmxPort; }

        public List<LogFileConfiguration> getLogFileConfigurations() { return logFileConfigurations; }
        public void setLogFileConfigurations(List<LogFileConfiguration> logFileConfigurations) {
            this.logFileConfigurations = logFileConfigurations;
        }

        public Map<String, String> getEnvironmentVariables() { return environmentVariables; }
        public void setEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables = environmentVariables;
        }

        public boolean isHotDeploymentEnabled() { return hotDeploymentEnabled; }
        public void setHotDeploymentEnabled(boolean hotDeploymentEnabled) {
            this.hotDeploymentEnabled = hotDeploymentEnabled;
        }

        public boolean isUpdateClassesAndResources() { return updateClassesAndResources; }
        public void setUpdateClassesAndResources(boolean updateClassesAndResources) {
            this.updateClassesAndResources = updateClassesAndResources;
        }

        public int getDeploymentTimeout() { return deploymentTimeout; }
        public void setDeploymentTimeout(int deploymentTimeout) { this.deploymentTimeout = deploymentTimeout; }

        public boolean isEnableAccessLog() { return enableAccessLog; }
        public void setEnableAccessLog(boolean enableAccessLog) { this.enableAccessLog = enableAccessLog; }

        public String getAccessLogPattern() { return accessLogPattern; }
        public void setAccessLogPattern(String accessLogPattern) { this.accessLogPattern = accessLogPattern; }
    }
}