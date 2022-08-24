package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class TomcatInfoComponent implements Disposable {

    private JPanel mainPanel;

    public TomcatInfoComponent(TomcatInfo tomcatInfo) {
        JBLabel versionLabel = new JBLabel(tomcatInfo.getVersion());
        JBLabel locationLabel = new JBLabel(tomcatInfo.getPath());
        mainPanel = FormBuilder.createFormBuilder()
                .setVerticalGap(UIUtil.LARGE_VGAP)
                .addLabeledComponent("Version:", versionLabel)
                .addLabeledComponent("Location:", locationLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        mainPanel.setBorder(JBUI.Borders.empty(0, 10));
    }

    public JComponent getMainPanel() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        mainPanel = null;
    }

}
