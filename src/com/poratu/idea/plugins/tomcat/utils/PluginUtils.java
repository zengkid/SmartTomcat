package com.poratu.idea.plugins.tomcat.utils;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public abstract class PluginUtils {

    public static Sdk getDefaultJDK(){
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        if (allJdks == null || allJdks.length == 0) {
            //todo: guide user to config the SDK
        }
        Sdk jdk = allJdks[0];
        return jdk;
    }

    public static TomcatInfo getTomcatInfo(String tomcatHome) {
        return getTomcatInfo(getDefaultJDK().getHomePath(), tomcatHome);
    }

    public static TomcatInfo getTomcatInfo(String javaHome, String tomcatHome) {
//        java -cp lib/catalina.jar org.apache.catalina.util.ServerInfo
        String command = javaHome + "/bin/java -cp " + tomcatHome + "/lib/catalina.jar org.apache.catalina.util.ServerInfo";
        BufferedReader reader = null;
        final TomcatInfo tomcatInfo = new TomcatInfo();
        tomcatInfo.setPath(tomcatHome);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Stream<String> lines = reader.lines();
            lines.forEach(s -> {
                if (s.startsWith("Server version")) {
                    String name = StringUtils.replace(getValue(s), "/", " ");
                    tomcatInfo.setName(name);
                } else if (s.startsWith("Server number")) {
                    String version = getValue(s);
                    tomcatInfo.setVersion(version);
                }

            });

            reader.close();

        } catch (Exception e) {
            return null;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tomcatInfo;

    }

    private static String getValue(String s) {
        String[] strings = StringUtils.split(s, ":");
        String result = "";
        if (strings != null && strings.length == 2) {
            result = strings[1].trim();
        }
        return result;


    }

    public static void main(String[] args) {
        String javaHome = "D:\\develop\\Java\\jdk1.8.0_45";
        String tomcatHome = "D:\\develop\\server\\tomcat8";
        TomcatInfo tomcatInfo = getTomcatInfo(javaHome, tomcatHome);
        System.out.println("tomcatInfo = " + tomcatInfo);
    }
}
