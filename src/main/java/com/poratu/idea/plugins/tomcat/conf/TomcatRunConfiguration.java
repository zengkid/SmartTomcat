package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class TomcatRunConfiguration extends LocatableConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {
    private TomcatInfo tomcatInfo;
    private String docBase;
    private String moduleName;
    private String contextPath;
    private String port = "8080";
    private String adminPort = "8005";
    private String vmOptions;
    private Map<String, String> envOptions;
    private Boolean passParentEnvironmentVariables = true;

    protected TomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        TomcatInfoConfigs applicationService = ServiceManager.getService(TomcatInfoConfigs.class);
        List<TomcatInfo> tomcatInfos = applicationService.getTomcatInfos();
        if (!tomcatInfos.isEmpty()) {
            this.tomcatInfo = tomcatInfos.get(0);
        }
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        Project project = getProject();
        return new TomcatSettingsEditor(this, project);
    }

    @Override
    public void checkSettingsBeforeRun() throws RuntimeConfigurationException {
        super.checkSettingsBeforeRun();
    }

    @Override
    public void checkConfiguration() {

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
                docBase = file.getCanonicalPath();
                moduleName = ModuleUtil.findModuleForFile(file, project).getName();
                contextPath = "/" + moduleName;
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
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(this, element);

    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(this, element);
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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

    public TomcatInfo getTomcatInfo() {
        return tomcatInfo;
    }

    public void setTomcatInfo(TomcatInfo tomcatInfo) {
        this.tomcatInfo = tomcatInfo;
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


    @Override
    @NotNull
    public Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        Module[] modules = moduleManager.getModules();
        return modules;
    }
}
