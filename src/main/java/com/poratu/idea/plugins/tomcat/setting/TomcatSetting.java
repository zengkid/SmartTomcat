package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.ui.JBUI;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Author : zengkid
 * Date   : 2017-02-25
 * Time   : 13:40
 */
public class TomcatSetting {
    private static final TomcatSetting tomcatSetting = new TomcatSetting();
    private JPanel mainPanel;
    private JList tomcatList;
    private JPanel tomcatListPanel;
    private JPanel tomcatSetupPanel;
    private JTextField tomcatNameField;
    private JTextField tomcatVersionField;
    private JTextField tomcatServerField;
    private boolean inited = false;

    private TomcatSetting() {

    }

    public static TomcatSetting getInstance() {
        return tomcatSetting;
    }

    public void initComponent() {
        if (!inited) {

            ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tomcatList)
                    .setToolbarPosition(ActionToolbarPosition.TOP)
                    .setPanelBorder(JBUI.Borders.empty());
            decorator.setAddAction(anActionButton -> {

                DefaultListModel<TomcatInfo> model = (DefaultListModel<TomcatInfo>) tomcatList.getModel();

                VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), null, null);
                if (virtualFile == null) { // cancel to choose file
                    return;
                }
                String presentableUrl = virtualFile.getPresentableUrl();


                TomcatInfo tomcatInfo = PluginUtils.getTomcatInfo(presentableUrl);
                int size = model.size();
                if (model.contains(tomcatInfo)) {
                    TomcatInfo[] infos = new TomcatInfo[size];
                    model.copyInto(infos);
                    int maxVersion = TomcatInfoConfigs.getInstance().getMaxVersion(tomcatInfo);
                    tomcatInfo.setNumber(maxVersion + 1);
                }
                model.add(size, tomcatInfo);
                tomcatList.setSelectedIndex(size);


            });

            tomcatListPanel.add(decorator.createPanel(), BorderLayout.CENTER);
            tomcatList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        TomcatInfo tomcatInfo = (TomcatInfo) tomcatList.getSelectedValue();
                        if (tomcatInfo != null) {
                            String tomcatName = tomcatInfo.getName();
                            if (tomcatInfo.getNumber() > 0) {
                                tomcatName = tomcatName + "(" + tomcatInfo.getNumber() + ")";
                            }
                            tomcatNameField.setText(tomcatName);
                            tomcatVersionField.setText(tomcatInfo.getVersion());
                            tomcatServerField.setText(tomcatInfo.getPath());
                        } else {
                            tomcatNameField.setText("");
                            tomcatVersionField.setText("");
                            tomcatServerField.setText("");
                        }
                    }

                }
            });

            inited = true;
        }

        DefaultListModel<TomcatInfo> model = new DefaultListModel<>();
        java.util.List<TomcatInfo> tomcatInfos = TomcatInfoConfigs.getInstance().getTomcatInfos();
        for (TomcatInfo tomcatInfo : tomcatInfos) {
            model.add(model.size(), tomcatInfo);
        }
        tomcatList.setModel(model);
        tomcatNameField.setText("");
        tomcatVersionField.setText("");
        tomcatServerField.setText("");
        if (tomcatList.getModel().getSize() > 0) {
            tomcatList.setSelectedIndex(0);
        }


    }


    public JPanel getMainPanel() {
        initComponent();
        return mainPanel;
    }

    public JList getTomcatList() {
        return tomcatList;
    }

}
