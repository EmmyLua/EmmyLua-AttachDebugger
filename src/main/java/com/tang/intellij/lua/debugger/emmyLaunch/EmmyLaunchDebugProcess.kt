package com.tang.intellij.lua.debugger.emmyLaunch

import com.google.gson.Gson
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.emmy.*
import com.tang.intellij.lua.debugger.utils.FileUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ThreadLocalRandom


class EmmyLaunchDebugProcess(
    session: XDebugSession,
    private val configuration: EmmyLaunchDebugConfiguration,
    val project: Project
) :
    EmmyDebugProcessBase(session) {
    private var toolProcessHandler: ColoredProcessHandler? = null;

    override fun setupTransporter() {
        LaunchDebug() { it ->
            attachTo(it)
            Thread.sleep(300)
            toolProcessHandler?.let { tool ->
                tool.processInput.write("connected\n".toByteArray())
                tool.processInput.flush()
            }
        }
    }

    private fun attachTo(pid: Int) {
        val port = getPort(pid)
        val transporter = SocketClientTransporter("localhost", port)
        transporter.handler = this
        transporter.logger = this
        this.transporter = transporter
        transporter.start()
    }

    private fun getPort(pid: Int): Int {
        var port = pid
        // 1024 - 65535
        while (port > 0xffff) port -= 0xffff
        while (port < 0x400) port += 0x400
        return port;
    }

    private fun detectArch(): EmmyWinArch {
        val tool = FileUtils.getPluginVirtualFile("debugger/bin/win32-x64/emmy_tool.exe")
        val commandLine = GeneralCommandLine(tool)
        commandLine.addParameters("arch_file", configuration.program)
        val process = commandLine.createProcess()
        process.waitFor()
        val exitValue = process.exitValue()
        return if (exitValue == 0) EmmyWinArch.X64 else EmmyWinArch.X86
    }

    private fun LaunchWithWindows() {
        val port = getPort(ThreadLocalRandom.current().nextInt(10240) + 10240);
        val arch = detectArch()
        val path = FileUtils.getPluginVirtualFile("debugger/bin/win32-${arch}")
        val re = Regex("[^/\\\\]+\$")
        val mc = re.find(configuration.program)

        val commandLine = GeneralCommandLine()
        commandLine.exePath = "wt"
        commandLine.setWorkDirectory(path)
        commandLine.addParameters(
            "--title",
            if (mc != null) mc.groups[0]?.value else configuration.program,
            "emmy_tool.exe",
            "run_and_attach",
            "-dll",
            "emmy_hook.dll",
            "-dir",
            "\"${path}\"",
            "-work",
            "\"${configuration.workingDirectory}\"",
            "-block-on-exit",
            "-exe",
            "\"${configuration.program}\"",
            "-debug-port",
            port.toString(),
            "-listen-mode",
            "-args",
            "\"${configuration.parameter}\""
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
    }

    private fun LaunchDebug(onConnected: (pid: Int) -> Unit) {
        val arch = detectArch()
        val path = FileUtils.getPluginVirtualFile("debugger/bin/win32-${arch}")

        val commandLine = GeneralCommandLine().apply {
            exePath = "${path}/emmy_tool.exe"
            setWorkDirectory(path)
            addParameter("launch")
            if (configuration.useWindowsTerminal) {
                addParameter("-create-new-window")
            }
            addParameters(
                "-dll",
                "emmy_hook.dll",
                "-dir",
                "\"${path}\"",
                "-work",
                "\"${configuration.workingDirectory}\"",
                "-exe",
                "\"${configuration.program}\"",
                "-args",
                "\"${configuration.parameter}\""
            )

            charset = Charset.forName("utf8")
        }

        var forOut = false;
        toolProcessHandler = ColoredProcessHandler(commandLine)
        toolProcessHandler?.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
            }

            override fun processTerminated(processEvent: ProcessEvent) {
                toolProcessHandler = null
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

                    ProcessOutputTypes.STDOUT -> {
                        if (!forOut) {
                            forOut = true
                            val pid = processEvent.text.trim().toInt()
                            onConnected(pid)
                            return
                        }
                        print(
                            processEvent.text,
                            LogConsoleType.NORMAL,
                            ConsoleViewContentType.SYSTEM_OUTPUT
                        )
                    }
                }
            }
        })

        toolProcessHandler?.startNotify()
    }

    override fun onDisconnect() {
        super.onDisconnect()
        toolProcessHandler?.processInput?.write("close\n".toByteArray())
        toolProcessHandler = null
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