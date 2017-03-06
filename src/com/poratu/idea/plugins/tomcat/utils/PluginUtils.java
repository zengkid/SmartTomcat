package com.poratu.idea.plugins.tomcat.utils;

import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public abstract class PluginUtils {
    public static TomcatInfo getTomcatInfo(String javaHome, String tomcatHome) {
//        java -cp lib/catalina.jar org.apache.catalina.util.ServerInfo
        String command = javaHome + "/bin/java -cp " + tomcatHome + "/lib/catalina.jar org.apache.catalina.util.ServerInfo";
        TomcatInfo tomcatInfo = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            Optional<String> first = reader.lines().filter(s -> s.startsWith("Server number")).findFirst();
            if (first.isPresent()) {
                tomcatInfo = new TomcatInfo();
                String tomcatVersion = first.get();
                String[] strings = tomcatVersion.split(":");
                tomcatInfo.setPath(tomcatHome);
                tomcatInfo.setName("Tomcat");
                tomcatInfo.setVersion(strings[1].trim());

            }
            reader.close();

        } catch (Exception e) {


        }
        return tomcatInfo;

    }

    public static void main(String[] args) {
        String javaHome = "D:\\develop\\Java\\jdk1.8.0_45";
        String tomcatHome = "D:\\develop\\server\\tomcat8";
        TomcatInfo tomcatInfo = getTomcatInfo(javaHome, tomcatHome);
        System.out.println("tomcatInfo = " + tomcatInfo);
    }
}
