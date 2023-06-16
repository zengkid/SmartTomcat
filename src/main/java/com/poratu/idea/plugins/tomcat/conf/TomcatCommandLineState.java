package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathsList;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:10 AM
 */

public class TomcatCommandLineState extends JavaCommandLineState {

    private static final String JDK_JAVA_OPTIONS = "JDK_JAVA_OPTIONS";
    private static final String ENV_JDK_JAVA_OPTIONS = "--add-opens=java.base/java.lang=ALL-UNNAMED " +
            "--add-opens=java.base/java.io=ALL-UNNAMED " +
            "--add-opens=java.base/java.util=ALL-UNNAMED " +
            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED";

    private static final String TOMCAT_MAIN_CLASS = "org.apache.catalina.startup.Bootstrap";
    private static final String PARAM_CATALINA_HOME = "catalina.home";
    private static final String PARAM_CATALINA_BASE = "catalina.base";
    private static final String PARAM_CATALINA_TMPDIR = "java.io.tmpdir";
    private static final String PARAM_LOGGING_CONFIG = "java.util.logging.config.file";
    private static final String PARAM_LOGGING_MANAGER = "java.util.logging.manager";
    private static final String PARAM_LOGGING_MANAGER_VALUE = "org.apache.juli.ClassLoaderLogManager";
    private TomcatRunConfiguration configuration;

    protected TomcatCommandLineState(@NotNull ExecutionEnvironment environment) {
        super(environment);
    }

    protected TomcatCommandLineState(ExecutionEnvironment environment, TomcatRunConfiguration configuration) {
        this(environment);
        this.configuration = configuration;
    }

    @Override
    protected GeneralCommandLine createCommandLine() throws ExecutionException {
        GeneralCommandLine commandLine = super.createCommandLine();

        // Set JDK_JAVA_OPTIONS
        String originalJdkJavaOptions = commandLine.getEnvironment().get(JDK_JAVA_OPTIONS);
        String jdkJavaOptions = originalJdkJavaOptions == null ? ENV_JDK_JAVA_OPTIONS : originalJdkJavaOptions + " " + ENV_JDK_JAVA_OPTIONS;
        return commandLine.withEnvironment(JDK_JAVA_OPTIONS, jdkJavaOptions);
    }

    @Override
    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler progressHandler = super.startProcess();
        if (progressHandler instanceof KillableProcessHandler) {
            boolean shouldKillSoftly = !DebuggerSettings.getInstance().KILL_PROCESS_IMMEDIATELY;
            ((KillableProcessHandler) progressHandler).setShouldKillProcessSoftly(shouldKillSoftly);
        }
        return progressHandler;
    }

    @Override
    protected JavaParameters createJavaParameters() {
        try {
            Path workingPath = PluginUtils.getWorkingPath(configuration);
            Module module = configuration.getModule();
            if (workingPath == null || module == null) {
                throw new ExecutionException("The Module Root specified is not a module according to Intellij");
            }

            Path tomcatInstallationPath = Paths.get(configuration.getTomcatInfo().getPath());
            Project project = configuration.getProject();
            String tomcatVersion = configuration.getTomcatInfo().getVersion();
            String vmOptions = configuration.getVmOptions();
            String extraClassPath = configuration.getExtraClassPath();
            Map<String, String> envOptions = configuration.getEnvOptions();

            // Copy the Tomcat configuration files to the working directory
            Path confPath = workingPath.resolve("conf");
            FileUtil.delete(confPath);
            FileUtil.createDirectory(confPath.toFile());
            FileUtil.copyDir(tomcatInstallationPath.resolve("conf").toFile(), confPath.toFile());
            // create the temp folder
            FileUtil.createDirectory(workingPath.resolve("temp").toFile());

            updateServerConf(confPath, configuration);
            createContextFile(tomcatVersion, module, confPath);
            deleteTomcatWorkFiles(workingPath);

            ProjectRootManager manager = ProjectRootManager.getInstance(project);

            JavaParameters javaParams = new JavaParameters();
            javaParams.setDefaultCharset(project);
            javaParams.setWorkingDirectory(workingPath.toFile());
            javaParams.setJdk(manager.getProjectSdk());

            javaParams.getClassPath().add(tomcatInstallationPath.resolve("bin/bootstrap.jar").toFile());
            javaParams.getClassPath().add(tomcatInstallationPath.resolve("bin/tomcat-juli.jar").toFile());
            if (StringUtil.isNotEmpty(extraClassPath)) {
                javaParams.getClassPath().addAll(StringUtil.split(extraClassPath, File.pathSeparator));
            }

            javaParams.setMainClass(TOMCAT_MAIN_CLASS);
            javaParams.getProgramParametersList().add("start");

            javaParams.setPassParentEnvs(configuration.isPassParentEnvs());
            if (envOptions != null) {
                javaParams.setEnv(envOptions);
            }

            ParametersList vmParams = javaParams.getVMParametersList();
            vmParams.addParametersString(vmOptions);
            vmParams.addProperty(PARAM_CATALINA_HOME, tomcatInstallationPath.toString());
            vmParams.defineProperty(PARAM_CATALINA_BASE, workingPath.toString());
            vmParams.defineProperty(PARAM_CATALINA_TMPDIR, workingPath.resolve("temp").toString());
            vmParams.defineProperty(PARAM_LOGGING_CONFIG, confPath.resolve("logging.properties").toString());
            vmParams.defineProperty(PARAM_LOGGING_MANAGER, PARAM_LOGGING_MANAGER_VALUE);

            return javaParams;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        return new ServerConsoleView(configuration);
    }

    private void updateServerConf(Path confPath, TomcatRunConfiguration cfg)
            throws ParserConfigurationException, XPathExpressionException, TransformerException, IOException, SAXException {
        Path serverXml = confPath.resolve("server.xml");
        Document doc = PluginUtils.createDocumentBuilder().parse(serverXml.toFile());
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression exprConnectorShutdown = xpath.compile("/Server[@shutdown='SHUTDOWN']");
        XPathExpression exprConnector = xpath.compile("/Server/Service[@name='Catalina']/Connector[@protocol='HTTP/1.1' and (not(@SSLEnabled) or @SSLEnabled='false')]");
        XPathExpression exprSSLConnector = xpath.compile("/Server/Service[@name='Catalina']/Connector[@protocol='HTTP/1.1' and @SSLEnabled='true']");
        XPathExpression exprContext = xpath.compile("/Server/Service[@name='Catalina']/Engine[@name='Catalina']/Host/Context");

        Element portShutdown = (Element) exprConnectorShutdown.evaluate(doc, XPathConstants.NODE);
        Element portE = (Element) exprConnector.evaluate(doc, XPathConstants.NODE);
        Element sslPortE = (Element) exprSSLConnector.evaluate(doc, XPathConstants.NODE);

        NodeList nodeList = (NodeList) exprContext.evaluate(doc, XPathConstants.NODESET);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
        }

        portShutdown.setAttribute("port", String.valueOf(cfg.getAdminPort()));
        portE.setAttribute("port", String.valueOf(cfg.getPort()));
        if (sslPortE != null) {
            sslPortE.setAttribute("port", String.valueOf(cfg.getSslPort()));
        }

        PluginUtils.createTransformer().transform(new DOMSource(doc), new StreamResult(serverXml.toFile()));
    }

    private void createContextFile(String tomcatVersion, Module module, Path confPath)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {
        String docBase = configuration.getDocBase();
        String contextPath = configuration.getContextPath();
        String normalizedContextPath = StringUtil.trim(contextPath, ch -> ch != '/');
        String contextFileName = StringUtil.defaultIfEmpty(normalizedContextPath, "ROOT").replace('/', '#');
        Path contextFilesDir = confPath.resolve("Catalina/localhost");
        Path contextFilePath = contextFilesDir.resolve(contextFileName + ".xml");

        // Create `conf/Catalina/localhost` folder
        FileUtil.createDirectory(contextFilesDir.toFile());

        DocumentBuilder builder = PluginUtils.createDocumentBuilder();
        Document doc = builder.newDocument();
        Element contextRoot = createContextElement(doc, builder);

        contextRoot.setAttribute("docBase", docBase);

        collectResources(doc, contextRoot, module, tomcatVersion);
        doc.appendChild(contextRoot);

        StringWriter writer = new StringWriter();
        PluginUtils.createTransformer().transform(new DOMSource(doc), new StreamResult(writer));
        FileUtil.writeToFile(contextFilePath.toFile(), writer.toString());
    }

    private Element createContextElement(Document doc, DocumentBuilder builder) throws IOException, SAXException {
        Path contextFile = findContextFileInApp();

        if (contextFile == null) {
            return doc.createElement("Context");
        }

        Element contextEl = builder.parse(contextFile.toFile()).getDocumentElement();
        return (Element) doc.importNode(contextEl, true);
    }

    private Path findContextFileInApp() {
        String docBase = configuration.getDocBase();
        if (docBase == null) {
            return null;
        }

        Path metaInf = Paths.get(docBase).resolve("META-INF");
        Path contextLocalFile = metaInf.resolve("context_local.xml");
        Path contextFile = metaInf.resolve("context.xml");

        if (Files.exists(contextLocalFile)) {
            return contextLocalFile;
        } else if (Files.exists(contextFile)) {
            return contextFile;
        } else {
            return null;
        }
    }

    private void collectResources(Document doc, Element contextRoot, Module module, String tomcatVersion) {
        String majorVersionStr = tomcatVersion.split("\\.")[0];
        int majorVersion = Integer.parseInt(majorVersionStr);
        PathsList pathsList = OrderEnumerator.orderEntries(module)
                .withoutSdk().runtimeOnly().productionOnly().getPathsList();

        if (pathsList.isEmpty()) {
            return;
        }

        if (majorVersion >= 8) {
            Element resources = createResourcesElementIfNecessary(doc, contextRoot);
            pathsList.getVirtualFiles().forEach(file -> {
                Element res;
                String tagName;
                String className;
                String webAppMount;

                if (file.isDirectory()) {
                    tagName = "PreResources";
                    className = "org.apache.catalina.webresources.DirResourceSet";
                    webAppMount = "/WEB-INF/classes";
                } else {
                    tagName = "PostResources";
                    className = "org.apache.catalina.webresources.FileResourceSet";
                    webAppMount = "/WEB-INF/lib/" + file.getName();
                }

                res = doc.createElement(tagName);
                res.setAttribute("base", file.getPath());
                res.setAttribute("className", className);
                res.setAttribute("webAppMount", webAppMount);

                resources.appendChild(res);
            });
        } else if (majorVersion >= 6) {
            Element loader = doc.createElement("Loader");
            loader.setAttribute("className", "org.apache.catalina.loader.VirtualWebappLoader");
            loader.setAttribute("virtualClasspath", StringUtil.join(pathsList.getPathList(), ";"));
            contextRoot.appendChild(loader);
        } else {
            throw new RuntimeException("Unsupported Tomcat version: " + tomcatVersion);
        }
    }

    private Element createResourcesElementIfNecessary(Document doc, Element contextRoot) {
        Element resources = (Element) contextRoot.getElementsByTagName("Resources").item(0);
        if (resources == null) {
            resources = doc.createElement("Resources");
            contextRoot.appendChild(resources);
        }

        if (Registry.is("smartTomcat.resources.allowLinking")) {
            resources.setAttribute("allowLinking", "true");
        }

        int cacheMaxSize = Registry.intValue("smartTomcat.resources.cacheMaxSize", 10240);
        if (cacheMaxSize > 0) {
            resources.setAttribute("cacheMaxSize", String.valueOf(cacheMaxSize));
        }

        return resources;
    }

    private void deleteTomcatWorkFiles(Path tomcatHome) {
        Path tomcatWorkPath = tomcatHome.resolve("work/Catalina/localhost");
        FileUtil.processFilesRecursively(tomcatWorkPath.toFile(), file -> {
            // Delete the work files except the session persistence files
            if (file.isFile() && !file.getName().endsWith(".ser")) {
                FileUtil.delete(file);
            }
            return true;
        });
    }

}
