package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class TomcatRunConfiguration extends LocatableConfigurationBase<TomcatRunConfigurationOptions> implements RunProfileWithCompileBeforeLaunchOption {
    private transient Module module;

    protected TomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        TomcatInfoConfigs applicationService = ApplicationManager.getApplication().getService(TomcatInfoConfigs.class);
        List<TomcatInfo> tomcatInfos = applicationService.getTomcatInfos();
        if (!tomcatInfos.isEmpty()) {
            getOptions().setTomcatInfo(tomcatInfos.get(0));
        }
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
                getOptions().setDocBase(file.getCanonicalPath());
                module = ModuleUtilCore.findModuleForFile(file, project);
                getOptions().setContextPath("/" + module.getName());
            }
        } catch (Exception e) {
            //do nothing.
        }

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new AppCommandLineState(executionEnvironment, this);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(getOptions(), element);

    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(getOptions(), element);
    }

    public Module getModule() {
        if (module == null) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(getOptions().getDocBase()));
            module = ModuleUtilCore.findModuleForFile(Objects.requireNonNull(virtualFile), this.getProject());
        }
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }


    public TomcatInfo getTomcatInfo() {
        return getOptions().getTomcatInfo();
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        getOptions().setTomcatInfo(tomcatInfo);
    }

    @Override
    public Module @NotNull [] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }

    @NotNull
    @Override
    protected TomcatRunConfigurationOptions getOptions() {
        return (TomcatRunConfigurationOptions) super.getOptions();
    }

    @NotNull
    @Override
    protected Class<? extends TomcatRunConfigurationOptions> getDefaultOptionsClass() {
        return TomcatRunConfigurationOptions.class;
    }


    public String getDocBase() {
        return getOptions().getDocBase();
    }

    public void setDocBase(String docBase) {
        getOptions().setDocBase(docBase);
    }

    public String getContextPath() {
        return getOptions().getContextPath();
    }

    public void setContextPath(String contextPath) {
        getOptions().setContextPath(contextPath);
    }

    public String getPort() {
        return getOptions().getPort();
    }

    public void setPort(String port) {
        getOptions().setPort(port);
    }

    public String getAdminPort() {
        return getOptions().getAdminPort();
    }

    public void setAdminPort(String adminPort) {
        getOptions().setAdminPort(adminPort);
    }

    public String getVmOptions() {
        return getOptions().getVmOptions();
    }

    public void setVmOptions(String vmOptions) {
        getOptions().setVmOptions(vmOptions);
    }

    public Map<String, String> getEnvOptions() {
        return getOptions().getEnvOptions();
    }

    public void setEnvOptions(Map<String, String> envOptions) {
        getOptions().setEnvOptions(envOptions);
    }

    public Boolean getPassParentEnvironmentVariables() {
        return getOptions().getPassParentEnvironmentVariables();
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvironmentVariables) {
        getOptions().setPassParentEnvironmentVariables(passParentEnvironmentVariables);
    }
}

class TomcatRunConfigurationOptions extends LocatableRunConfigurationOptions {

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

