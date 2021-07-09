package com.tang.intellij.lua.debugger.emmy

// work around 等intellij-idea 加上以后删掉

class BreakPointEx(val file: String, val line: Int, val condition: String?) {
}

class AddBreakPointReqEx(val breakPoints: List<BreakPointEx>) : Message(MessageCMD.AddBreakPointReq)

