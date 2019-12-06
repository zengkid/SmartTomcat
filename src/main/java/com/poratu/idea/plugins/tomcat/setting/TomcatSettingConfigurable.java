package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:14
 */
public class TomcatSettingConfigurable implements Configurable {


    @Nls
    @Override
    public String getDisplayName() {
        return "Tomcat Server";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Smart Tomcat Help";
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        TomcatSetting tomcatSetting = TomcatSetting.getInstance();
        return tomcatSetting.getMainPanel();

    }

    @Override
    public boolean isModified() {
        TomcatSetting tomcatSetting = TomcatSetting.getInstance();
        JList tomcatList = tomcatSetting.getTomcatList();
        DefaultListModel<TomcatInfo> model = (DefaultListModel) tomcatList.getModel();
        List<TomcatInfo> tomcatInfos = TomcatInfoConfigs.getInstance().getTomcatInfos();

        if (model.size() != tomcatInfos.size()) {
            return true;
        }

        for (int i = 0; i < tomcatInfos.size(); i++) {
            TomcatInfo info1 = tomcatInfos.get(i);
            TomcatInfo info2 = model.elementAt(i);
            if (!info1.equals(info2)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply() {
        TomcatSetting tomcatSetting = TomcatSetting.getInstance();
        JList tomcatList = tomcatSetting.getTomcatList();
        DefaultListModel<TomcatInfo> model = (DefaultListModel) tomcatList.getModel();


        List<TomcatInfo> tomcatInfos = TomcatInfoConfigs.getInstance().getTomcatInfos();
        tomcatInfos.clear();
        Enumeration<TomcatInfo> elements = model.elements();
        while (elements.hasMoreElements()) {
            TomcatInfo tomcatInfo = elements.nextElement();
            tomcatInfos.add(tomcatInfo);
        }

    }

    @Override
    public void reset() {
//        TomcatInfoConfigs.getInstance().getTomcatInfos().clear();
    }
}
