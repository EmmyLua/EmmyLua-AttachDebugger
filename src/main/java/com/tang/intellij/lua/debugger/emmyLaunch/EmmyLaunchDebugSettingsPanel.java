package com.tang.intellij.lua.debugger.emmyLaunch;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EmmyLaunchDebugSettingsPanel extends SettingsEditor<EmmyLaunchDebugConfiguration> implements DocumentListener {
    private JTextField Program;
    private JTextField WorkingDirectory;
    private JTextField Parameters;
    private JCheckBox useWindowsTerminalCheckBox;
    private JPanel panel;

    public EmmyLaunchDebugSettingsPanel(Project project) {
        Program.getDocument().addDocumentListener(this);
        WorkingDirectory.getDocument().addDocumentListener(this);
        Parameters.getDocument().addDocumentListener(this);
        useWindowsTerminalCheckBox.addActionListener(e -> onChanged());
    }


    @Override
    protected void resetEditorFrom(@NotNull EmmyLaunchDebugConfiguration emmyLaunchDebugConfiguration) {
        Program.setText(emmyLaunchDebugConfiguration.getProgram());
        WorkingDirectory.setText(emmyLaunchDebugConfiguration.getWorkingDirectory());
        Parameters.setText(emmyLaunchDebugConfiguration.getParameter());
        useWindowsTerminalCheckBox.setSelected(emmyLaunchDebugConfiguration.getUseWindowsTerminal());
    }

    @Override
    protected void applyEditorTo(@NotNull EmmyLaunchDebugConfiguration emmyLaunchDebugConfiguration) throws ConfigurationException {
        emmyLaunchDebugConfiguration.setProgram(Program.getText());
        emmyLaunchDebugConfiguration.setWorkingDirectory(WorkingDirectory.getText());
        emmyLaunchDebugConfiguration.setParameter(Parameters.getText());
        emmyLaunchDebugConfiguration.setUseWindowsTerminal(useWindowsTerminalCheckBox.isSelected());
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
