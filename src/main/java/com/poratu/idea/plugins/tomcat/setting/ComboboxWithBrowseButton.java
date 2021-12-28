package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;

import javax.swing.*;
import java.awt.*;

/**
 * Author : zengkid
 * Date   : 2021-12-28
 * Time   : 21:37
 *
 * com.intellij.ui.ComboboxWithBrowseButton is Deprecated, and scheduled for removal in 2022.3
 * In order to be used in future versions, so created a copy of <b>com.intellij.ui.ComboboxWithBrowseButton</b>
 */
public class ComboboxWithBrowseButton extends ComponentWithBrowseButton<JComboBox> {
    public ComboboxWithBrowseButton() {
        super(new JComboBox(), null);
    }

    public ComboboxWithBrowseButton(JComboBox comboBox) {
        super(comboBox, null);
    }

    public JComboBox getComboBox() {
        return getChildComponent();
    }

    @Override
    public void setTextFieldPreferredWidth(final int charCount) {
        super.setTextFieldPreferredWidth(charCount);
        final Component comp = getChildComponent().getEditor().getEditorComponent();
        Dimension size = comp.getPreferredSize();
        FontMetrics fontMetrics = comp.getFontMetrics(comp.getFont());
        size.width = fontMetrics.charWidth('a') * charCount;
        comp.setPreferredSize(size);
    }

    public void addBrowseFolderListener(Project project, FileChooserDescriptor descriptor) {
        addBrowseFolderListener(null, null, project, descriptor, TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT);
    }
}