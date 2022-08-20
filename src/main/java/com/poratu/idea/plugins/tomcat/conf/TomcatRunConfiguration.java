package com.poratu.idea.plugins.tomcat.conf;

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
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
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

    private transient Module module;

    protected TomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        TomcatInfoConfigs applicationService = ApplicationManager.getApplication().getService(TomcatInfoConfigs.class);
        List<TomcatInfo> tomcatInfos = applicationService.getTomcatInfos();
        if (!tomcatInfos.isEmpty()) {
            tomcatOptions.setTomcatInfo(tomcatInfos.get(0));
        }
        addPredefinedTomcatLogFiles();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        Project project = getProject();
        TomcatSettingsEditor tomcatSetting = new TomcatSettingsEditor(this, project);
        SettingsEditorGroup<TomcatRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), tomcatSetting);
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
        return group;
    }

    @Override
    public void onNewConfigurationCreated() {
        super.onNewConfigurationCreated();

        try {
            Project project = getProject();

            ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
            VirtualFile[] sourceRoots = rootManager.getContentSourceRoots();

            Optional<VirtualFile> webinfFile = Stream.of(sourceRoots).map(VirtualFile::getParent).distinct().flatMap(f ->
                    Stream.of(f.getChildren()).filter(c -> {
                        Path path = Paths.get(c.getCanonicalPath(), "WEB-INF");
                        return path.toFile().exists();
                    })).distinct().findFirst();


            if (webinfFile.isPresent()) {
                VirtualFile file = webinfFile.get();
                tomcatOptions.setDocBase(file.getCanonicalPath());
                module = ModuleUtilCore.findModuleForFile(file, project);
                tomcatOptions.setContextPath("/" + module.getName());
            }
        } catch (Exception e) {
            //do nothing.
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
        return new TomcatCommandLineState(executionEnvironment, this);
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
        XmlSerializer.deserializeInto(tomcatOptions, element);

        if (getAllLogFiles().isEmpty()) {
            addPredefinedTomcatLogFiles();
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(tomcatOptions, element);
    }

    private void addPredefinedTomcatLogFiles() {
        createPredefinedLogFiles().forEach(this::addPredefinedLogFile);
    }

    @Nullable
    public Module getModule() {
        if (module != null) {
            return module;
        }

        if (tomcatOptions.getDocBase() != null) {
            VirtualFile virtualFile = VfsUtil.findFile(Paths.get(tomcatOptions.getDocBase()), true);
            if (virtualFile != null) {
                module = ReadAction.compute(() -> ModuleUtilCore.findModuleForFile(virtualFile, getProject()));
            }
        }
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public TomcatInfo getTomcatInfo() {
        return tomcatOptions.getTomcatInfo();
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        tomcatOptions.setTomcatInfo(tomcatInfo);
    }

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

    public String getPort() {
        return tomcatOptions.getPort();
    }

    public void setPort(String port) {
        tomcatOptions.setPort(port);
    }

    public String getAdminPort() {
        return tomcatOptions.getAdminPort();
    }

    public void setAdminPort(String adminPort) {
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

    public Boolean getPassParentEnvironmentVariables() {
        return tomcatOptions.getPassParentEnvironmentVariables();
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvironmentVariables) {
        tomcatOptions.setPassParentEnvironmentVariables(passParentEnvironmentVariables);
    }

}


class TomcatRunConfigurationOptions {

    private TomcatInfo tomcatInfo;

    private String docBase;
    private String contextPath;
    private String port = "8080";
    private String adminPort = "8005";
    private String vmOptions;
    private Map<String, String> envOptions;
    private Boolean passParentEnvironmentVariables = true;

    public TomcatInfo getTomcatInfo() {
        return tomcatInfo;
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        this.tomcatInfo = tomcatInfo;
    }

    @Nullable
    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(String adminPort) {
        this.adminPort = adminPort;
    }

    public String getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
    }

    public Map<String, String> getEnvOptions() {
        return envOptions;
    }

    public void setEnvOptions(Map<String, String> envOptions) {
        this.envOptions = envOptions;
    }

    public Boolean getPassParentEnvironmentVariables() {
        return passParentEnvironmentVariables;
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvironmentVariables) {
        this.passParentEnvironmentVariables = passParentEnvironmentVariables;
    }

}
