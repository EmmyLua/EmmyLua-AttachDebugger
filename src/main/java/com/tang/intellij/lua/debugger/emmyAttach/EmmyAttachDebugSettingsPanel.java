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

    private JPanel panel;
    private JTextField ProcessName;
    private JTextField Encoding;

    private JRadioButton UsePid;
    private JRadioButton UseProcessName;
    private JPanel AttachMode;
    private ButtonGroup AttachModeGroup;

    public EmmyAttachDebugSettingsPanel(Project project) {
        ProcessId.getDocument().addDocumentListener(this);
        ProcessName.getDocument().addDocumentListener(this);

        Encoding.setText("gbk");
        Encoding.getDocument().addDocumentListener(this);

        AttachModeGroup = new ButtonGroup();
        AttachModeGroup.add(UsePid);
        AttachModeGroup.add(UseProcessName);
        UsePid.addChangeListener(e -> onChanged());
        UseProcessName.addChangeListener(e -> onChanged());
    }


    @Override
    protected void resetEditorFrom(@NotNull EmmyAttachDebugConfiguration configuration) {
        ProcessId.setText(configuration.getPid());
        ProcessName.setText(configuration.getProcessName());
        Encoding.setText(configuration.getEncoding());

        if(configuration.getAttachMode() == EmmyAttachMode.Pid){
            UsePid.setSelected(true);
        }
        else if(configuration.getAttachMode() == EmmyAttachMode.ProcessName){
            UseProcessName.setSelected(true);
        }

    }

    @Override
    protected void applyEditorTo(@NotNull EmmyAttachDebugConfiguration configuration) throws ConfigurationException {
        configuration.setPid(ProcessId.getText());
        configuration.setProcessName(ProcessName.getText());
        configuration.setEncoding(Encoding.getText());

        if (UsePid.isSelected()) {
            configuration.setAttachMode(EmmyAttachMode.Pid);
        } else if (UseProcessName.isSelected()) {
            configuration.setAttachMode(EmmyAttachMode.ProcessName);
        }
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
