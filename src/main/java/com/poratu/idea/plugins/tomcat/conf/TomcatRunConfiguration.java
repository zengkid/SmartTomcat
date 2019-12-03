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
    private String moduleName;
    private String contextPath;
    private String port;
    private String ajpPort;
    private String adminPort;
    private String vmOptions;
    private Map<String, String> envOptions;
    private Boolean passParentEnvironmentVariables;
    private String className;
    private String debug;
    private String digest;
    private String roleNameCol;
    private String userCredCol;
    private String userNameCol;
    private String userRoleTable;
    private String userTable;
    private String jndiGlobal;
    private String jndiName;
    private String jndiType;
    private String dataSourceName;


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
        this.tomcatInfo = TomcatInfoConfigs.getInstance().getCurrent();
//        this.tomcatInstallation = PropertiesComponent.getInstance().getValue("TOMCAT_INSTALLATION");
//        this.tomcatInstallation = JDOMExternalizerUtil.readField(element, "TOMCAT_INSTALLATION");
        this.docBase = JDOMExternalizerUtil.readField(element, "DOC_BASE");
        this.moduleName = JDOMExternalizerUtil.readField(element, "DOC_MODULE_ROOT");
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
        this.className = JDOMExternalizerUtil.readField(element, "CLASS_NAME");
        this.debug = JDOMExternalizerUtil.readField(element, "DEBUG");
        this.digest = JDOMExternalizerUtil.readField(element, "DIGEST");
        this.roleNameCol = JDOMExternalizerUtil.readField(element, "ROLE_NAME_COL");
        this.userCredCol = JDOMExternalizerUtil.readField(element, "USER_CRED_COL");
        this.userNameCol = JDOMExternalizerUtil.readField(element, "USER_NAME_COL");
        this.userRoleTable = JDOMExternalizerUtil.readField(element, "USER_ROLE_TABLE");
        this.userTable = JDOMExternalizerUtil.readField(element, "USER_TABLE");
        this.jndiGlobal = JDOMExternalizerUtil.readField(element, "JNDI_GLOBAL");
        this.jndiName = JDOMExternalizerUtil.readField(element, "JNDI_NAME");
        this.jndiType = JDOMExternalizerUtil.readField(element, "JNDI_TYPE");
        this.dataSourceName = JDOMExternalizerUtil.readField(element, "DATA_SOURCE_NAME");
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        TomcatInfoConfigs.getInstance().setCurrent(tomcatInfo);
//        JDOMExternalizerUtil.writeField(element, "TOMCAT_INSTALLATION", tomcatInstallation);
        JDOMExternalizerUtil.writeField(element, "DOC_BASE", docBase);
        JDOMExternalizerUtil.writeField(element, "DOC_MODULE_ROOT", moduleName);
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
        JDOMExternalizerUtil.writeField(element, "CLASS_NAME", className);
        JDOMExternalizerUtil.writeField(element, "DEBUG", debug);
        JDOMExternalizerUtil.writeField(element, "DIGEST", digest);
        JDOMExternalizerUtil.writeField(element, "ROLE_NAME_COL", roleNameCol);
        JDOMExternalizerUtil.writeField(element, "USER_CRED_COL", userCredCol);
        JDOMExternalizerUtil.writeField(element, "USER_NAME_COL", userNameCol);
        JDOMExternalizerUtil.writeField(element, "USER_ROLE_TABLE", userRoleTable);
        JDOMExternalizerUtil.writeField(element, "USER_TABLE", userTable);
        JDOMExternalizerUtil.writeField(element, "JNDI_GLOBAL", jndiGlobal);
        JDOMExternalizerUtil.writeField(element, "JNDI_NAME", jndiName);
        JDOMExternalizerUtil.writeField(element, "JNDI_TYPE", jndiType);
        JDOMExternalizerUtil.writeField(element, "DATA_SOURCE_NAME", dataSourceName);
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getRoleNameCol() {
        return roleNameCol;
    }

    public void setRoleNameCol(String roleNameCol) {
        this.roleNameCol = roleNameCol;
    }

    public String getUserCredCol() {
        return userCredCol;
    }

    public void setUserCredCol(String userCredCol) {
        this.userCredCol = userCredCol;
    }

    public String getUserNameCol() {
        return userNameCol;
    }

    public void setUserNameCol(String userNameCol) {
        this.userNameCol = userNameCol;
    }

    public String getUserRoleTable() {
        return userRoleTable;
    }

    public void setUserRoleTable(String userRoleTable) {
        this.userRoleTable = userRoleTable;
    }

    public String getUserTable() {
        return userTable;
    }

    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    public String getJndiGlobal() {
        return jndiGlobal;
    }

    public void setJndiGlobal(String jndiGlobal) {
        this.jndiGlobal = jndiGlobal;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getJndiType() {
        return jndiType;
    }

    public void setJndiType(String jndiType) {
        this.jndiType = jndiType;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @NotNull
    @Override
    public Module[] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }
}
