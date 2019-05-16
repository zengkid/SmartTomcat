package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class TomcatRunConfiguration extends RunConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {
    private TomcatInfo tomcatInfo;
    //    private String tomcatInstallation;
    private String docBase;
    private String docModuleRoot;
    private String contextPath;
    private String port;
    private String ajpPort;
    private String adminPort;
    private String vmOptions;
    private Map<String, String> envOptions;
    private Boolean passParentEnvironmentVariables;


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
        this.docModuleRoot = JDOMExternalizerUtil.readField(element, "DOC_MODULE_ROOT");
        this.contextPath = JDOMExternalizerUtil.readField(element, "CONTEXT_PATH");
        this.port = JDOMExternalizerUtil.readField(element, "TOMCAT_PORT");
        this.ajpPort = JDOMExternalizerUtil.readField(element, "AJP_PORT");
        this.adminPort = JDOMExternalizerUtil.readField(element, "ADMIN_PORT");
        this.vmOptions = JDOMExternalizerUtil.readField(element, "VM_OPTIONS");
        if(envOptions == null) {
            envOptions = new HashMap<String, String>();
        }
        EnvironmentVariablesComponent.readExternal(element, envOptions);
        this.passParentEnvironmentVariables = Boolean.valueOf(JDOMExternalizerUtil.readField(element, "PASS_PARENT_ENV_OPTIONS"));
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        TomcatInfoConfigs.getInstance(getProject()).setCurrent(tomcatInfo);
//        JDOMExternalizerUtil.writeField(element, "TOMCAT_INSTALLATION", tomcatInstallation);
        JDOMExternalizerUtil.writeField(element, "DOC_BASE", docBase);
        JDOMExternalizerUtil.writeField(element, "DOC_MODULE_ROOT", docModuleRoot);
        JDOMExternalizerUtil.writeField(element, "CONTEXT_PATH", contextPath);
        JDOMExternalizerUtil.writeField(element, "TOMCAT_PORT", port);
        JDOMExternalizerUtil.writeField(element, "AJP_PORT", ajpPort);
        JDOMExternalizerUtil.writeField(element, "ADMIN_PORT", adminPort);
        JDOMExternalizerUtil.writeField(element, "VM_OPTIONS", vmOptions);
        if(envOptions != null && !envOptions.isEmpty()) {
            EnvironmentVariablesComponent.writeExternal(element, envOptions);
        }
        if(passParentEnvironmentVariables == null) {
            passParentEnvironmentVariables = Boolean.TRUE;
        }
        JDOMExternalizerUtil.writeField(element, "PASS_PARENT_ENV_OPTIONS", "" + passParentEnvironmentVariables);
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public String getDocModuleRoot() {
        return docModuleRoot;
    }

    public void setDocModuleRoot(String docModuleRoot) {
        this.docModuleRoot = docModuleRoot;
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

    public String getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(String ajpPort) {
        this.ajpPort = ajpPort;
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

    public Map<String, String> getEnvOptions() { return envOptions; }

    public void setEnvOptions(Map<String, String> envOptions) {
        this.envOptions = envOptions;
    }

    public Boolean getPassParentEnvironmentVariables() {
        return passParentEnvironmentVariables;
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvironmentVariables) {
        this.passParentEnvironmentVariables = passParentEnvironmentVariables;
    }

    @NotNull
    @Override
    public Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }
}
