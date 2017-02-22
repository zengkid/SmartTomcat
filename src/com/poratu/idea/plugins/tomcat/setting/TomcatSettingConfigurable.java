package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
        return "help tomcat plugin";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return new JLabel("hello");
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }
}
