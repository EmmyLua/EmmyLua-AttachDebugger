package com.tang.intellij.lua.debugger.utils

import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ide.plugins.PluginManagerCore
import java.io.File

object FileUtils {
    private val pluginVirtualDirectory: VirtualFile?
        get() {
            val descriptor = PluginManagerCore.getPlugin(PluginId.getId("com.tang.emmylua.attach-debugger"))
            if (descriptor != null) {
                val pluginPath = descriptor.path

                val url = VfsUtil.pathToUrl(pluginPath.absolutePath)

                return VirtualFileManager.getInstance().findFileByUrl(url)
            }

            return null
        }

    val archExeFile: String?
        get() = getPluginVirtualFile("debugger/bin/win32-x86/emmy_tool.exe")

    fun getPluginVirtualFile(path: String): String? {
        val directory = pluginVirtualDirectory
        if (directory != null) {
            var fullPath = directory.path + "/classes/" + path
            if (File(fullPath).exists())
                return fullPath
            fullPath = directory.path + "/" + path
            if (File(fullPath).exists())
                return fullPath
        }
        return null
    }
}