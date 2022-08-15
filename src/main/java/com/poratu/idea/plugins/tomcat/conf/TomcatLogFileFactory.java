package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TomcatLogFileFactory {

    public static final String TOMCAT_LOCALHOST_LOG_ID = "Tomcat Localhost";
    public static final String TOMCAT_CATALINA_LOG_ID = "Tomcat Catalina";
    public static final String TOMCAT_ACCESS_LOG_ID = "Tomcat Localhost Access";
    public static final String TOMCAT_MANAGER_LOG_ID = "Tomcat Manager";
    public static final String TOMCAT_HOST_MANAGER_LOG_ID = "Tomcat Host Manager";

    private final String id;
    private final String filename;
    private boolean enabled;

    public TomcatLogFileFactory(String id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public TomcatLogFileFactory(String id, String filename, boolean enabled) {
        this(id, filename);
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public LogFileOptions createOptions(PredefinedLogFile file, @Nullable Path logsDirPath) {
        Path logsPath = logsDirPath == null ? Paths.get("logs") : logsDirPath;
        return new LogFileOptions(file.getId() + " Log", logsPath.resolve(filename) + ".*", file.isEnabled());
    }

    public PredefinedLogFile createPredefinedLogFile() {
        return new PredefinedLogFile(id, enabled);
    }

}
