package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class TomcatRunConfiguration extends RunConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {
    private TomcatInfo tomcatInfo;
    //    private String tomcatInstallation;
    private String docBase;
    private String contextPath;
    private String port;
    private String vmOptions;
    private String envOptions;


    protected TomcatRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
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
    public void checkConfiguration() throws RuntimeConfigurationException {

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new AppCommandLineState(executionEnvironment, this);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        this.tomcatInfo = TomcatInfoConfigs.getInstance(getProject()).getCurrent();
//        this.tomcatInstallation = PropertiesComponent.getInstance().getValue("TOMCAT_INSTALLATION");
//        this.tomcatInstallation = JDOMExternalizerUtil.readField(element, "TOMCAT_INSTALLATION");
        this.docBase = JDOMExternalizerUtil.readField(element, "DOC_BASE");
        this.contextPath = JDOMExternalizerUtil.readField(element, "CONTEXT_PATH");
        this.port = JDOMExternalizerUtil.readField(element, "TOMCAT_PORT");
        this.vmOptions = JDOMExternalizerUtil.readField(element, "VM_OPTIONS");
        this.envOptions = JDOMExternalizerUtil.readField(element, "ENV_OPTIONS");
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        TomcatInfoConfigs.getInstance(getProject()).setCurrent(tomcatInfo);
//        JDOMExternalizerUtil.writeField(element, "TOMCAT_INSTALLATION", tomcatInstallation);
        JDOMExternalizerUtil.writeField(element, "DOC_BASE", docBase);
        JDOMExternalizerUtil.writeField(element, "CONTEXT_PATH", contextPath);
        JDOMExternalizerUtil.writeField(element, "TOMCAT_PORT", port);
        JDOMExternalizerUtil.writeField(element, "VM_OPTIONS", vmOptions);
        JDOMExternalizerUtil.writeField(element, "ENV_OPTIONS", envOptions);
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

    public String getEnvOptions() { return envOptions; }

    public void setEnvOptions(String envOptions) {
        this.envOptions = envOptions;
    }

    @NotNull
    @Override
    public Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }
}
