package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TomcatLogFile {

    public static final String TOMCAT_LOCALHOST_LOG_ID = "Tomcat Localhost Log";
    public static final String TOMCAT_CATALINA_LOG_ID = "Tomcat Catalina Log";
    public static final String TOMCAT_ACCESS_LOG_ID = "Tomcat Access Log";
    public static final String TOMCAT_MANAGER_LOG_ID = "Tomcat Manager Log";
    public static final String TOMCAT_HOST_MANAGER_LOG_ID = "Tomcat Host Manager Log";

    private final String id;
    private final String filename;
    private boolean enabled;

    public TomcatLogFile(String id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public TomcatLogFile(String id, String filename, boolean enabled) {
        this(id, filename);
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public LogFileOptions createLogFileOptions(PredefinedLogFile file, @Nullable Path logsDirPath) {
        Path logsPath = logsDirPath == null ? Paths.get("logs") : logsDirPath;
        return new LogFileOptions(file.getId(), logsPath.resolve(filename) + ".*", file.isEnabled());
    }

    public PredefinedLogFile createPredefinedLogFile() {
        return new PredefinedLogFile(id, enabled);
    }

}
