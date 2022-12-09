package com.tang.intellij.lua.debugger.emmyLaunch

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.tang.intellij.lua.debugger.LuaCommandLineState
import com.tang.intellij.lua.debugger.LuaConfigurationFactory
import com.tang.intellij.lua.debugger.LuaRunConfiguration
import com.tang.intellij.lua.lang.LuaIcons
import org.jdom.Element
import javax.swing.Icon


class EmmyLaunchConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return LuaIcons.FILE
    }

    override fun getConfigurationTypeDescription(): String {
        return "Emmy Launch debugger"
    }

    override fun getId(): String {
        return "lua.emmyLaunch.debugger"
    }

    override fun getDisplayName(): String {
        return "Emmy Launch debugger"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf( EmmyLaunchDebuggerConfigurationFactory(this))
    }
}


class EmmyLaunchDebuggerConfigurationFactory(val type: EmmyLaunchConfigurationType) : LuaConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return EmmyLaunchDebugConfiguration(project, this)
    }
}

class EmmyLaunchDebugConfiguration(project: Project, factory: EmmyLaunchDebuggerConfigurationFactory) : LuaRunConfiguration(project, factory),
    RunConfigurationWithSuppressedDefaultRunAction {
    var program = "lua"
    var workingDirectory = ""
    var parameter = ""
    var useWindowsTerminal = false

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<EmmyLaunchDebugConfiguration>()
        group.addEditor("emmy", EmmyLaunchDebugSettingsPanel(project))
        return group
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return LuaCommandLineState(environment)
    }

    override fun getValidModules(): Collection<Module> {
        return emptyList()
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizerUtil.writeField(element, "Program", program)
        JDOMExternalizerUtil.writeField(element, "WorkingDirectory", workingDirectory)
        JDOMExternalizerUtil.writeField(element, "Parameter", parameter)
        JDOMExternalizerUtil.writeField(element, "UseWindowsTerminal", useWindowsTerminal.toString())
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        JDOMExternalizerUtil.readField(element, "Program")?.let {
            program = it
        }
        JDOMExternalizerUtil.readField(element, "WorkingDirectory")?.let {
            workingDirectory = it
        }
        JDOMExternalizerUtil.readField(element, "Parameter")?.let {
            parameter = it
        }
        JDOMExternalizerUtil.readField(element, "UseWindowsTerminal")?.let { value ->
            useWindowsTerminal = value == "true"
        }
    }


}