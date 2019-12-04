package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class TomcatRunConfiguration extends RunConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {
    private TomcatInfo tomcatInfo;
    private String docBase;
    private String moduleName;
    private String contextPath;
    private String port;
    private String ajpPort;
    private String adminPort;
    private String vmOptions;
    private Map<String, String> envOptions;
    private Boolean passParentEnvironmentVariables = true;
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
    public void checkConfiguration() {

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
