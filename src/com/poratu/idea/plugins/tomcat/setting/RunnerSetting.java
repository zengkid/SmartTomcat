package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:13
 */
public class RunnerSetting {
    private JPanel mainPanel;
    private ComboboxWithBrowseButton tomcatField;
    private TextFieldWithBrowseButton docBaseField;
    private JTextField contextPathField;
    private JFormattedTextField portField;
    private JXButton configrationButton;
    private RawCommandLineEditor vmOptons;
    private RawCommandLineEditor envOptions;
    private Project project;

    public RunnerSetting(Project project) {
        this.project = project;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public ComboboxWithBrowseButton getTomcatField() {
        return tomcatField;
    }

    public TextFieldWithBrowseButton getDocBaseField() {
        return docBaseField;
    }

    public JTextField getContextPathField() {
        return contextPathField;
    }

    public JFormattedTextField getPortField() {
        return portField;
    }


    public JXButton getConfigrationButton() {
        return configrationButton;
    }

    public RawCommandLineEditor getVmOptons() {
        return vmOptons;
    }

    public RawCommandLineEditor getEnvOptions() {
        return envOptions;
    }

    private void createUIComponents() {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();


        tomcatField = new ComboboxWithBrowseButton();
        JComboBox<TomcatInfo> comboBox = tomcatField.getComboBox();

        List<TomcatInfo> tomcatInfos = TomcatInfoConfigs.getInstance().getTomcatInfos();
        CollectionComboBoxModel<TomcatInfo> aModel = new CollectionComboBoxModel<>(tomcatInfos);
        comboBox.setModel(aModel);

        tomcatField.addBrowseFolderListener("Tomcat Server", "Please choose tomcat server path", project, fileChooserDescriptor, new TextComponentAccessor<JComboBox>() {
            public String getText(JComboBox comboBox) {
                Object item = comboBox.getEditor().getItem();
                return item.toString();
            }

            public void setText(JComboBox comboBox, @NotNull String text) {
//                comboBox.getEditor().setItem(text);
                TomcatInfo tomcatInfo = PluginUtils.getTomcatInfo(text);

                if (tomcatInfo != null) {

                    CollectionComboBoxModel<TomcatInfo> model = (CollectionComboBoxModel) comboBox.getModel();

                    if (model.contains(tomcatInfo)) {
                        int maxVersion = TomcatInfoConfigs.getInstance().getMaxVersion(tomcatInfo);
                        tomcatInfo.setNumber(maxVersion + 1);
                    }

                    model.add(model.getSize(), tomcatInfo);
                    model.setSelectedItem(tomcatInfo);

                }
            }
        });
    }


}
