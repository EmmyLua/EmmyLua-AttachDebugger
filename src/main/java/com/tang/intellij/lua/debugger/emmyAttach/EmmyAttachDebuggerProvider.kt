/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.debugger.emmyAttach

import com.intellij.execution.process.ProcessInfo
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.UserDataHolder
import com.intellij.xdebugger.attach.XAttachDebuggerProvider
import com.intellij.xdebugger.attach.XAttachHost
import com.intellij.xdebugger.attach.XLocalAttachDebugger
import com.tang.intellij.lua.debugger.utils.FileUtils
import com.tang.intellij.lua.debugger.utils.ProcessDetailInfo
import com.tang.intellij.lua.debugger.utils.listProcesses

class EmmyAttachDebuggerProvider : XAttachDebuggerProvider {
    companion object {
        val DETAIL_KEY = Key.create<Map<Int, ProcessDetailInfo>>("LuaLocalAttachDebuggerProvider.key")
    }

    private var processMap = mapOf<Int, ProcessDetailInfo>()

    override fun isAttachHostApplicable(p0: XAttachHost): Boolean {
        return true
    }

    override fun getAvailableDebuggers(project: Project, attachHost: XAttachHost , processInfo: ProcessInfo, userDataHolder: UserDataHolder): List<XLocalAttachDebugger> {
        if (!SystemInfoRt.isWindows)
            return emptyList()
        if (userDataHolder.getUserData(DETAIL_KEY) == null) {
            val archExe = FileUtils.archExeFile
            if (archExe == null) {
                ApplicationManager.getApplication().invokeLater {
                    val notification = Notification(
                            "Emmylua",
                            "Error",
                            "Debugging tool 'emmy_tool.exe' has been removed, please reinstall the 'emmylua' plugin",
                            NotificationType.WARNING)
                    notification.isImportant = true
                    Notifications.Bus.notify(notification)
                }
            }
            processMap = listProcesses()
            userDataHolder.putUserData(DETAIL_KEY, processMap)
        }

        if (processInfo.executableName.endsWith(".exe")) {
            val list = mutableListOf<EmmyAttachDebugger>()
            val info = processMap[processInfo.pid]
            if (info != null && info.path.isNotEmpty()) {
                list.add(EmmyAttachDebugger(processInfo, info))
            }
            return list
        }
        return emptyList()
    }

    override fun getPresentationGroup() = EmmyAttachGroup.instance

}