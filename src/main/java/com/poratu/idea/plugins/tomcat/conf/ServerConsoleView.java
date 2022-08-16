package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import org.jetbrains.annotations.NotNull;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class ServerConsoleView extends ConsoleViewImpl {
    private final TomcatRunConfiguration configuration;
    private boolean printStarted = false;

    public ServerConsoleView(TomcatRunConfiguration configuration) {
        super(configuration.getProject(), true);
        this.configuration = configuration;
    }

    @Override
    public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
        super.print(s, contentType);

        if (printStarted) {
            return;
        }

        // skip the exception log e.g.:
        // at org.apache.catalina.startup.Catalina.start(Catalina.java:772)
        boolean isExceptionLog = s.trim().startsWith("at ");
        if (isExceptionLog) {
            return;
        }

        if (s.contains("org.apache.catalina.startup.Catalina start")
                || s.contains("org.apache.catalina.startup.Catalina.start")) {
            String url = "http://localhost" + (configuration.getPort().equals("80") ? "" : ":" + configuration.getPort()) + configuration.getContextPath();
            super.print(url + "\n", contentType);
            printStarted = true;
        }
    }

}
