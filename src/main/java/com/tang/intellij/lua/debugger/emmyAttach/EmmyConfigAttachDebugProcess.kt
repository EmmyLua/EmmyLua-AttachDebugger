package com.tang.intellij.lua.debugger.emmyAttach

import com.google.gson.Gson
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.emmy.*
import com.tang.intellij.lua.debugger.utils.FileUtils
import com.tang.intellij.lua.debugger.utils.ProcessDetailInfo
import com.tang.intellij.lua.debugger.utils.listProcessesByEncoding


class EmmyConfigAttachDebugProcess(
    session: XDebugSession,
    val configuration: EmmyAttachDebugConfiguration
) : EmmyDebugProcessBase(session) {

    private var pid: Int = 0

    override fun sessionInitialized() {
        val attachMode = configuration.attachMode


        if (attachMode == EmmyAttachMode.Pid) {
            pid = configuration.pid.toInt()
            return super.sessionInitialized()
        }

        val processName = configuration.processName
        val processes = listProcessesByEncoding(configuration.encoding)
        val attachableList = mutableListOf<ProcessDetailInfo>()
        for (info in processes) {
            if (info.title.indexOf(processName) != -1 || info.path.indexOf(processName) != -1) {
                attachableList.add(info)
            }
        }

        if (attachableList.size == 1) {
            pid = attachableList.first().pid
            return super.sessionInitialized()
        }

        val jbInstance = JBPopupFactory.getInstance()
        val displayMap = mutableMapOf<String, ProcessDetailInfo>()

        for (processDetailInfo in attachableList) {
            displayMap["${processDetailInfo.pid}:${processDetailInfo.title}"] = processDetailInfo
        }

        jbInstance.createPopupChooserBuilder(displayMap.keys.toList())
            .setTitle("choose best match process")
            .setMovable(true)
            .setItemChosenCallback {
                val processDetailInfo = displayMap[it]
                if (processDetailInfo != null) {
                    pid = processDetailInfo.pid
                }
                super.sessionInitialized()
            }.createPopup().showInFocusCenter()
    }

    override fun setupTransporter() {
        val suc = attach()
        if (!suc) {
            session.stop()
            return
        }
        var port = pid
        // 1024 - 65535
        while (port > 0xffff) port -= 0xffff
        while (port < 0x400) port += 0x400

        val transporter = SocketClientTransporter("localhost", port)
        transporter.handler = this
        transporter.logger = this
        this.transporter = transporter
        transporter.start()
    }

    private fun detectArchByPid(pid: Int): EmmyWinArch {
        val tool = FileUtils.getPluginVirtualFile("debugger/bin/win32-x86/emmy_tool.exe")
        val commandLine = GeneralCommandLine(tool)
        commandLine.addParameters("arch_pid", "$pid")
        val process = commandLine.createProcess()
        process.waitFor()
        val exitValue = process.exitValue()
        return if (exitValue == 0) EmmyWinArch.X64 else EmmyWinArch.X86
    }

    private fun attach(): Boolean {
        val arch = detectArchByPid(pid)
        val path = FileUtils.getPluginVirtualFile("debugger/bin/win32-${arch}")
        val commandLine = GeneralCommandLine("${path}/emmy_tool.exe")
        commandLine.addParameters(
            "attach",
            "-p",
            "${pid}",
            "-dir",
            path,
            "-dll",
            "emmy_hook.dll"
        )
        val handler = OSProcessHandler(commandLine)
        handler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
            }

            override fun processTerminated(processEvent: ProcessEvent) {
            }

            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
            }

            override fun onTextAvailable(processEvent: ProcessEvent, key: Key<*>) {
                when (key) {
                    ProcessOutputTypes.STDERR -> print(
                        processEvent.text,
                        LogConsoleType.NORMAL,
                        ConsoleViewContentType.ERROR_OUTPUT
                    )
                    ProcessOutputTypes.STDOUT -> print(
                        processEvent.text,
                        LogConsoleType.NORMAL,
                        ConsoleViewContentType.SYSTEM_OUTPUT
                    )
                }
            }
        })
        handler.startNotify()
        handler.waitFor()
        return handler.exitCode == 0
    }

    override fun onReceiveMessage(cmd: MessageCMD, json: String) {
        if (cmd == MessageCMD.AttachedNotify) {
            val msg = Gson().fromJson(json, AttachedNotify::class.java)
            println(
                "Attached to lua state 0x${msg.state.toString(16)}",
                LogConsoleType.NORMAL,
                ConsoleViewContentType.SYSTEM_OUTPUT
            )
        } else super.onReceiveMessage(cmd, json)
    }
}