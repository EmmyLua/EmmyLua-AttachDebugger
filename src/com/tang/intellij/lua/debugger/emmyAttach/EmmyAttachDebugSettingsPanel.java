package com.tang.intellij.lua.debugger.emmyAttach;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.tang.intellij.lua.debugger.utils.ProcessDetailInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EmmyAttachDebugSettingsPanel extends SettingsEditor<EmmyAttachDebugConfiguration> implements DocumentListener {
    private JTextField ProcessId;
//    private JCheckBox captureProcessLogCheckBox;
    private JPanel panel;
//    private JList<ProcessDetailInfo> ProcessList;

    private ButtonGroup UseType;

    public EmmyAttachDebugSettingsPanel(Project project) {
        ProcessId.getDocument().addDocumentListener(this);
//        DefaultListModel<ProcessDetailInfo> model = new DefaultListModel<>();
//        ProcessList.setModel(model);

//        captureProcessLogCheckBox.addActionListener(e -> onChanged());
    }


    @Override
    protected void resetEditorFrom(@NotNull EmmyAttachDebugConfiguration configuration) {
        ProcessId.setText(configuration.getPid());

//        captureProcessLogCheckBox.setSelected(configuration.getCaptureLog());
    }

    @Override
    protected void applyEditorTo(@NotNull EmmyAttachDebugConfiguration configuration) throws ConfigurationException {
        configuration.setPid(ProcessId.getText());
//        configuration.setCaptureLog(captureProcessLogCheckBox.isSelected());
    }

    @Override
    protected @NotNull
    JComponent createEditor() {
        return panel;
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        onChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        onChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        onChanged();
    }

    private void onChanged() {
        fireEditorStateChanged();
    }
}
