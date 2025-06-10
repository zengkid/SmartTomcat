package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class ServerConsoleView extends ConsoleViewImpl {
    private final EnhancedTomcatRunConfiguration configuration;
    private boolean printStarted = false;
    private final List<String> httpPorts = new ArrayList<>();
    private final List<String> httpsPorts = new ArrayList<>();

    public ServerConsoleView(EnhancedTomcatRunConfiguration configuration) {
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

        if (this.parsePorts(s)) {
            return;
        }

        if (s.contains("org.apache.catalina.startup.Catalina start")
                || s.contains("org.apache.catalina.startup.Catalina.start")) {
            boolean portNotFound = httpPorts.isEmpty() && httpsPorts.isEmpty();
            // Use the configured port if the port is not found in the log
            if (portNotFound) {
                this.httpPorts.add(String.valueOf(configuration.getPort()));
                Integer sslPort = configuration.getSslPort();
                if (sslPort != null) {
                    this.httpsPorts.add(String.valueOf(sslPort));
                }
            }

            List<Url> urls = buildServerUrls();
            for (Url url : urls) {
                super.print(url + "\n", contentType);
            }
            printStarted = true;
        }
    }

    // Parse the port number from the log
    // 21-Jun-2023 13:27:15.385 INFO [main] org.apache.coyote.AbstractProtocol.init Initializing ProtocolHandler ["http-nio-8080"]
    // 21-Jun-2023 13:27:15.385 INFO [main] org.apache.coyote.AbstractProtocol.init Initializing ProtocolHandler ["https-jsse-nio-8443"]
    private boolean parsePorts(String s) {
        Pattern pattern = Pattern.compile("http-nio-(\\d+)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            String port = matcher.group(1);
            if (!this.httpPorts.contains(port)) {
                this.httpPorts.add(port);
            }
            return true;
        }

        pattern = Pattern.compile("https-jsse-nio-(\\d+)");
        matcher = pattern.matcher(s);
        if (matcher.find()) {
            String port = matcher.group(1);
            if (!this.httpsPorts.contains(port)) {
                this.httpsPorts.add(port);
            }
            return true;
        }

        return false;
    }

    private List<Url> buildServerUrls() {
        List<Url> urls = new ArrayList<>();
        String path = '/' + StringUtil.trimStart(configuration.getContextPath(), "/");

        for (String httpPort : httpPorts) {
            boolean isDefaultPort = "80".equals(httpPort);
            String authority = "localhost" + (isDefaultPort ? "" : ":" + httpPort);
            urls.add(Urls.newHttpUrl(authority, path));
        }

        for (String httpsPort : httpsPorts) {
            boolean isDefaultPort = "443".equals(httpsPort);
            String authority = "localhost" + (isDefaultPort ? "" : ":" + httpsPort);
            urls.add(Urls.newUrl("https", authority, path));
        }

        return urls;
    }
}
