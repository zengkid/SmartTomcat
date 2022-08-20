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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
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
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
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

    private static final Logger LOG = Logger.getInstance(TomcatCommandLineState.class);

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
            String contextPath = configuration.getContextPath();
            String tomcatVersion = configuration.getTomcatInfo().getVersion();
            String vmOptions = configuration.getVmOptions();
            Map<String, String> envOptions = configuration.getEnvOptions();

            // copy the Tomcat configuration files to the working directory
            Path confPath = workingPath.resolve("conf");
            FileUtil.createDirectory(confPath.toFile());
            FileUtil.copyFileOrDir(tomcatInstallationPath.resolve("conf").toFile(), confPath.toFile());

            updateServerConf(confPath, configuration);
            createContextFile(tomcatVersion, module, confPath, configuration.getDocBase(), contextPath);

            ProjectRootManager manager = ProjectRootManager.getInstance(project);

            JavaParameters javaParams = new JavaParameters();
            javaParams.setWorkingDirectory(workingPath.toFile());
            javaParams.setJdk(manager.getProjectSdk());
            javaParams.setDefaultCharset(project);
            javaParams.setMainClass(TOMCAT_MAIN_CLASS);
            javaParams.getProgramParametersList().add("start");
            addJarsInFolder(tomcatInstallationPath.resolve("bin"), javaParams);
            addJarsInFolder(tomcatInstallationPath.resolve("lib"), javaParams);

            javaParams.setPassParentEnvs(configuration.getPassParentEnvironmentVariables());
            if (envOptions != null) {
                javaParams.setEnv(envOptions);
            }

            ParametersList vmParams = javaParams.getVMParametersList();
            vmParams.addParametersString(vmOptions);
            vmParams.defineProperty(PARAM_CATALINA_HOME, tomcatInstallationPath.toString());
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

        DocumentBuilder builder = PluginUtils.createDocumentBuilder();
        Document doc = builder.parse(serverXml.toFile());
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression exprConnectorShutdown = xpath.compile("/Server[@shutdown='SHUTDOWN']");
        XPathExpression exprConnector = xpath.compile("/Server/Service[@name='Catalina']/Connector[@protocol='HTTP/1.1']");
        XPathExpression exprContext = xpath.compile
                ("/Server/Service[@name='Catalina']/Engine[@name='Catalina']/Host/Context");

        Element portShutdown = (Element) exprConnectorShutdown.evaluate(doc, XPathConstants.NODE);
        Element portE = (Element) exprConnector.evaluate(doc, XPathConstants.NODE);
        NodeList nodeList = (NodeList) exprContext.evaluate(doc, XPathConstants.NODESET);

        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
        }
        portShutdown.setAttribute("port", cfg.getAdminPort());
        portE.setAttribute("port", cfg.getPort());

        Source source = new DOMSource(doc);
        StreamResult result = new StreamResult(serverXml.toFile());
        PluginUtils.createTransformer().transform(source, result);
    }

    private void createContextFile(String tomcatVersion, Module module, Path confPath, String docBase, String contextPath)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {
        String normalizedContextPath = StringUtil.trimStart(contextPath, "/");
        Path catalinaContextPath = confPath.resolve("Catalina");
        Path contextFilesDir = catalinaContextPath.resolve("localhost");
        Path contextFilePath = contextFilesDir.resolve(normalizedContextPath + ".xml");

        try {
            // Delete `conf/Catalina` folder to clean up any existing context files
            FileUtil.delete(catalinaContextPath);
        } catch (IOException e) {
            LOG.warn("Could not delete " + catalinaContextPath.toAbsolutePath(), e);
        }

        // Create `conf/Catalina/localhost` folder
        FileUtil.createDirectory(contextFilesDir.toFile());

        DocumentBuilder builder = PluginUtils.createDocumentBuilder();
        Document doc = builder.newDocument();
        Element root;

        Path contextFile = findContextFileInApp();
        if (contextFile == null) {
            root = doc.createElement("Context");
        } else {
            Element contextEl = builder.parse(contextFile.toFile()).getDocumentElement();
            root = (Element) doc.importNode(contextEl, true);
        }

        root.setAttribute("docBase", docBase);
        root.setAttribute("path", "/" + normalizedContextPath);

        Element resources = collectResources(doc, module, tomcatVersion);
        if (resources != null) {
            root.appendChild(resources);
        }

        doc.appendChild(root);

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(contextFilePath.toFile());
        PluginUtils.createTransformer().transform(source, result);
    }

    @Nullable
    private Path findContextFileInApp() {
        if (configuration.getDocBase() == null) {
            return null;
        }

        Path metaInf = Paths.get(configuration.getDocBase()).resolve("META-INF");
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

    private Element collectResources(Document doc, @NotNull Module module, String tomcatVersion) {
        int majorVersion = Integer.parseInt(StringUtil.substringBefore(tomcatVersion, "."));
        PathsList pathsList = OrderEnumerator.orderEntries(module)
                .withoutSdk().runtimeOnly().productionOnly().getPathsList();

        if (pathsList.isEmpty()) {
            return null;
        }

        if (majorVersion >= 8) {
            Element resources = doc.createElement("Resources");
            pathsList.getVirtualFiles().forEach(file -> {
                Element res;
                if (file.isDirectory()) {
                    res = doc.createElement("PreResources");
                    res.setAttribute("className", "org.apache.catalina.webresources.DirResourceSet");
                    res.setAttribute("webAppMount", "/WEB-INF/classes");
                } else {
                    res = doc.createElement("PostResources");
                    res.setAttribute("className", "org.apache.catalina.webresources.FileResourceSet");
                    res.setAttribute("webAppMount", "/WEB-INF/lib/" + file.getName());
                }
                res.setAttribute("base", file.getPath());
                resources.appendChild(res);
            });

            return resources;
        }

        if (majorVersion >= 6) {
            Element loader = doc.createElement("Loader");
            loader.setAttribute("className", "org.apache.catalina.loader.VirtualWebappLoader");
            loader.setAttribute("virtualClasspath", pathsList.getPathsString());

            return loader;
        }

        return null;
    }

    private void addJarsInFolder(Path folder, JavaParameters javaParams) throws ExecutionException {
        // Dynamically adds the tomcat jars to the classpath
        if (!Files.exists(folder)) {
            throw new ExecutionException("The Tomcat installation configured doesn't contains a " + folder.getFileName() + " folder");
        }
        String[] jars = folder.toFile().list((dir, name) -> name.endsWith(".jar"));

        assert jars != null;
        for (String jarFile : jars) {
            javaParams.getClassPath().add(folder.resolve(jarFile).toFile().getAbsolutePath());
        }
    }

}
