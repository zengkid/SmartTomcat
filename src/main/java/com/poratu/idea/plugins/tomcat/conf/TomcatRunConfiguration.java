package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionBundle;
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
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Complete DevTomcat Run Configuration
 * Enhanced with Phase 1 logging integration
 */
public class TomcatRunConfiguration extends LocatableConfigurationBase<LocatableRunConfigurationOptions> implements RunProfileWithCompileBeforeLaunchOption {

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

    private TomcatRunConfigurationOptions tomcatOptions = new TomcatRunConfigurationOptions();
    private RunConfigurationModule configurationModule;

    protected TomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        configurationModule = new RunConfigurationModule(project);

        System.out.println("DevTomcat: TomcatRunConfiguration constructor called");

        try {
            TomcatServerManagerState applicationService = ApplicationManager.getApplication().getService(TomcatServerManagerState.class);
            List<TomcatInfo> tomcatInfos = applicationService.getTomcatInfos();
            System.out.println("DevTomcat: Found " + tomcatInfos.size() + " Tomcat servers");

            if (!tomcatInfos.isEmpty()) {
                tomcatOptions.setTomcatInfo(tomcatInfos.get(0));
                System.out.println("DevTomcat: Using Tomcat server: " + tomcatInfos.get(0).getName());
            } else {
                System.out.println("DevTomcat: WARNING - No Tomcat servers configured!");
            }

            addPredefinedTomcatLogFiles();
            System.out.println("DevTomcat: TomcatRunConfiguration created successfully");

        } catch (Exception e) {
            System.err.println("DevTomcat: Error in constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        Project project = getProject();

        // ALWAYS use Ultimate-style 5-tab interface for DevTomcat!
        System.out.println("DevTomcat: Using Ultimate-style 5-tab interface");

        SettingsEditorGroup<TomcatRunConfiguration> group = new SettingsEditorGroup<>();

        // Tab 1: Server Configuration (Ultimate-style)
        com.poratu.idea.plugins.tomcat.ui.ServerConfigurationTab serverTab =
                new com.poratu.idea.plugins.tomcat.ui.ServerConfigurationTab(project);

        group.addEditor("Server", new SettingsEditor<TomcatRunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
                serverTab.resetFrom(configuration);
            }

            @Override
            protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
                serverTab.applyTo(configuration);
            }

            @Override
            protected @NotNull javax.swing.JComponent createEditor() {
                return serverTab;
            }
        });

        // Tab 2: Deployment Configuration (Ultimate-style)
        com.poratu.idea.plugins.tomcat.ui.DeploymentConfigurationTab deploymentTab =
                new com.poratu.idea.plugins.tomcat.ui.DeploymentConfigurationTab(project);

        group.addEditor("Deployment", new SettingsEditor<TomcatRunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
                deploymentTab.resetFrom(configuration);
            }

            @Override
            protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
                deploymentTab.applyTo(configuration);
            }

            @Override
            protected @NotNull javax.swing.JComponent createEditor() {
                return deploymentTab;
            }
        });

        // Tab 3: Logs Configuration (Ultimate-style)
        com.poratu.idea.plugins.tomcat.ui.LogsConfigurationTab logsTab =
                new com.poratu.idea.plugins.tomcat.ui.LogsConfigurationTab(project, this);

        group.addEditor("Logs", new SettingsEditor<TomcatRunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
                logsTab.resetFrom(configuration);
            }

            @Override
            protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
                logsTab.applyTo(configuration);
            }

            @Override
            protected @NotNull javax.swing.JComponent createEditor() {
                return logsTab;
            }
        });

        // Tab 4: Startup/Connection (Ultimate-style)
        com.poratu.idea.plugins.tomcat.ui.StartupConnectionTab startupTab =
                new com.poratu.idea.plugins.tomcat.ui.StartupConnectionTab(project, this);

        group.addEditor("Startup/Connection", new SettingsEditor<TomcatRunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
                startupTab.resetFrom(configuration);
            }

            @Override
            protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
                startupTab.applyTo(configuration);
            }

            @Override
            protected @NotNull javax.swing.JComponent createEditor() {
                return startupTab;
            }
        });

        // Tab 5: Code Coverage (Ultimate-style)
        com.poratu.idea.plugins.tomcat.ui.CodeCoverageTab coverageTab =
                new com.poratu.idea.plugins.tomcat.ui.CodeCoverageTab(project);

        group.addEditor("Code Coverage", new SettingsEditor<TomcatRunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
                coverageTab.resetFrom(configuration);
            }

            @Override
            protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws com.intellij.openapi.options.ConfigurationException {
                coverageTab.applyTo(configuration);
            }

            @Override
            protected @NotNull javax.swing.JComponent createEditor() {
                return coverageTab;
            }
        });

        // Add Java extensions
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);

        System.out.println("DevTomcat: Ultimate-style interface created with 5 tabs: Server, Deployment, Logs, Startup/Connection, Code Coverage");
        return group;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        System.out.println("DevTomcat: Checking configuration...");

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

        if (getPort() == null || getAdminPort() == null) {
            System.err.println("DevTomcat: Ports not configured");
            throw new RuntimeConfigurationError("Port cannot be empty");
        }

        System.out.println("DevTomcat: Configuration check passed!");
    }

    @Override
    public void onNewConfigurationCreated() {
        super.onNewConfigurationCreated();

        try {
            Project project = getProject();
            List<VirtualFile> webRoots = PluginUtils.findWebRoots(project);

            if (!webRoots.isEmpty()) {
                VirtualFile webRoot = webRoots.get(0);
                tomcatOptions.setDocBase(webRoot.getPath());
                Module module = PluginUtils.findContainingModule(webRoot.getPath(), project);

                if (module == null) {
                    module = PluginUtils.guessModule(project);
                }

                if (module != null) {
                    tomcatOptions.setContextPath("/" + PluginUtils.extractContextPath(module));
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
        XmlSerializer.deserializeInto(element, tomcatOptions);
        configurationModule.readExternal(element);

        if (getAllLogFiles().isEmpty()) {
            addPredefinedTomcatLogFiles();
        }

        if (configurationModule.getModule() == null) {
            configurationModule.setModule(PluginUtils.findContainingModule(tomcatOptions.getDocBase(), getProject()));
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeObjectInto(tomcatOptions, element);
        if (configurationModule.getModule() != null) {
            configurationModule.writeExternal(element);
        }
    }

    private void addPredefinedTomcatLogFiles() {
        createPredefinedLogFiles().forEach(this::addPredefinedLogFile);
    }

    @Nullable
    public Module getModule() {
        return this.configurationModule.getModule();
    }

    public void setModule(Module module) {
        this.configurationModule.setModule(module);
    }

    public TomcatInfo getTomcatInfo() {
        return tomcatOptions.getTomcatInfo();
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        tomcatOptions.setTomcatInfo(tomcatInfo);
    }

    public String getCatalinaBase() { return tomcatOptions.getCatalinaBase(); }
    public void setCatalinaBase(String catalinaBase) { tomcatOptions.setCatalinaBase(catalinaBase); }

    public String getDocBase() {
        return tomcatOptions.getDocBase();
    }

    public void setDocBase(String docBase) {
        tomcatOptions.setDocBase(docBase);
    }

    public String getContextPath() {
        return tomcatOptions.getContextPath();
    }

    public void setContextPath(String contextPath) {
        tomcatOptions.setContextPath(contextPath);
    }

    public Integer getPort() {
        return tomcatOptions.getPort();
    }

    public void setPort(Integer port) {
        tomcatOptions.setPort(port);
    }

    public Integer getSslPort() {
        return tomcatOptions.getSslPort();
    }

    public void setSslPort(Integer sslPort) {
        tomcatOptions.setSslPort(sslPort);
    }

    public Integer getAdminPort() {
        return tomcatOptions.getAdminPort();
    }

    public void setAdminPort(Integer adminPort) {
        tomcatOptions.setAdminPort(adminPort);
    }

    public String getVmOptions() {
        return tomcatOptions.getVmOptions();
    }

    public void setVmOptions(String vmOptions) {
        tomcatOptions.setVmOptions(vmOptions);
    }

    public Map<String, String> getEnvOptions() {
        return tomcatOptions.getEnvOptions();
    }

    public void setEnvOptions(Map<String, String> envOptions) {
        tomcatOptions.setEnvOptions(envOptions);
    }

    public Boolean isPassParentEnvs() {
        return tomcatOptions.isPassParentEnvs();
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvs) {
        tomcatOptions.setPassParentEnvs(passParentEnvs);
    }

    public String getExtraClassPath() {
        return tomcatOptions.getExtraClassPath();
    }

    public void setExtraClassPath(String extraClassPath) {
        tomcatOptions.setExtraClassPath(extraClassPath);
    }

    // Note: setName(String) is inherited from parent class and is final

    @Override
    public RunConfiguration clone() {
        TomcatRunConfiguration clone = (TomcatRunConfiguration) super.clone();
        clone.configurationModule = new RunConfigurationModule(getProject());
        clone.configurationModule.setModule(configurationModule.getModule());
        clone.tomcatOptions = XmlSerializerUtil.createCopy(tomcatOptions);
        return clone;
    }

    private static class TomcatRunConfigurationOptions implements Serializable {
        private TomcatInfo tomcatInfo;
        private String catalinaBase;
        private String docBase;
        private String contextPath;
        private Integer port = 8080;
        private Integer sslPort;
        private Integer adminPort = 8005;
        private String vmOptions;
        private Map<String, String> envOptions;
        private Boolean passParentEnvs = true;
        private String extraClassPath;

        public TomcatInfo getTomcatInfo() { return tomcatInfo; }
        public void setTomcatInfo(TomcatInfo tomcatInfo) { this.tomcatInfo = tomcatInfo; }

        @Nullable
        public String getCatalinaBase() { return this.catalinaBase; }
        public void setCatalinaBase(String catalinaBase) { this.catalinaBase = catalinaBase; }

        @Nullable
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
    }
}