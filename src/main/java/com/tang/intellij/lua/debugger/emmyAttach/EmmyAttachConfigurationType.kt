package com.tang.intellij.lua.debugger.emmyAttach

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.tang.intellij.lua.debugger.LuaCommandLineState
import com.tang.intellij.lua.debugger.LuaRunConfiguration
import com.tang.intellij.lua.lang.LuaIcons
import org.jdom.Element
import javax.swing.Icon
import com.tang.intellij.lua.debugger.LuaConfigurationFactory
import com.tang.intellij.lua.debugger.emmy.EmmyDebugTransportType

class EmmyAttachConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return LuaIcons.FILE
    }

    override fun getConfigurationTypeDescription(): String {
        return "Emmy Attach Debugger"
    }

    override fun getId(): String {
        return "lua.emmyAttach.debugger"
    }

    override fun getDisplayName(): String {
        return "Emmy Attach Debugger"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(EmmyAttachDebuggerConfigurationFactory(this))
    }
}

enum class EmmyAttachMode(private val desc: String) {
    Pid("Pid"),
    ProcessName("ProcessName");

    override fun toString(): String {
        return desc;
    }
}

class EmmyAttachDebuggerConfigurationFactory(val type: EmmyAttachConfigurationType) : LuaConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return EmmyAttachDebugConfiguration(project, this)
    }
}

class EmmyAttachDebugConfiguration(project: Project, factory: EmmyAttachDebuggerConfigurationFactory) :
    LuaRunConfiguration(project, factory),
    RunConfigurationWithSuppressedDefaultRunAction {
    var attachMode = EmmyAttachMode.Pid;
    var pid = "0"
    var processName = ""
    var encoding = "gbk"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<EmmyAttachDebugConfiguration>()
        group.addEditor("emmy", EmmyAttachDebugSettingsPanel(project))
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
        JDOMExternalizerUtil.writeField(element, "AttachMode", attachMode.ordinal.toString())
        JDOMExternalizerUtil.writeField(element, "Pid", pid)
        JDOMExternalizerUtil.writeField(element, "ProcessName", processName)
        JDOMExternalizerUtil.writeField(element, "Encoding", encoding)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        JDOMExternalizerUtil.readField(element, "AttachMode")?.let { value ->
            val i = value.toInt()
            attachMode = EmmyAttachMode.values().find { it.ordinal == i } ?: EmmyAttachMode.Pid
        }
        JDOMExternalizerUtil.readField(element, "Pid")?.let {
            pid = it
        }
        JDOMExternalizerUtil.readField(element, "ProcessName")?.let {
            processName = it
        }
        JDOMExternalizerUtil.readField(element, "Encoding")?.let {
            encoding = it
        }
    }
}