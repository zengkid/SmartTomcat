package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.build.BuildTextConsoleView;
import com.intellij.build.events.impl.FailureImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class ServerConsoleView extends BuildTextConsoleView implements ConsoleView {
    private TomcatRunConfiguration configuration;
    private boolean printStarted = false;
    private FileChannel fileChannel;


    public ServerConsoleView(TomcatRunConfiguration configuration) {
        this(configuration.getProject(), true);
        this.configuration = configuration;
        Path logPath = PluginUtils.getWorkPath(configuration).resolve("logs");
        File logFile = logPath.toFile();
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        try {
            String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
            fileChannel = FileChannel.open(logPath.resolve("console." + date + ".log"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            super.append(new FailureImpl(e.getMessage(), e));
        }
    }

    public ServerConsoleView(Project project, boolean viewer) {
        super(project, viewer);
    }

    @Override
    public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
        super.print(s, contentType);
        try {
            fileChannel.write(ByteBuffer.wrap(s.getBytes()));
        } catch (Exception e) {
            super.append(new FailureImpl(e.getMessage(), e));
        }
        if (!printStarted) {
            if (s.contains("Server startup in") || s.contains("后服务器启动")) {
                String url = "http://localhost" + (configuration.getPort().equals("80") ? "" : ":" + configuration.getPort()) + configuration.getContextPath();
                super.print(url + "\n", contentType);
                printStarted = true;
            }
        }
    }


}
