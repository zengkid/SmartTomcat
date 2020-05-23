package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class ServerConsoleView extends ConsoleViewImpl implements ConsoleView {
    private TomcatRunConfiguration configuration;
    private boolean printStarted = false;


    public ServerConsoleView(TomcatRunConfiguration configuration) {
        this(configuration.getProject(), true);
        this.configuration = configuration;
    }

    public ServerConsoleView(Project project, boolean viewer) {
        super(project, viewer);
    }

    @Override
    public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
        super.print(s, contentType);
        if (!printStarted) {
            if (s.contains("Server startup in") || s.contains("后服务器启动")) {
                String url = "http://localhost" + (configuration.getPort().equals("80") ? "" : ":" + configuration.getPort()) + configuration.getContextPath();
                super.print(url + "\n", contentType);
                printStarted = true;
            }
        }
    }
}
