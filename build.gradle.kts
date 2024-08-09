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

import de.undercouch.gradle.tasks.download.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.2"
    id("de.undercouch.download").version("5.3.0")
}

repositories {
    mavenCentral()
}

intellij {
    pluginName.set("EmmyLua-AttachDebugger")
    version.set("2023.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.tang:1.4.8-IDEA231"))
}

val emmyluaDebuggerVersion = "1.8.2"
val emmyluaDebuggerProjectUrl = "https://github.com/EmmyLua/EmmyLuaDebugger"

task("downloadDebugger", type = Download::class) {
    src(arrayOf(
        "${emmyluaDebuggerProjectUrl }/releases/download/${emmyluaDebuggerVersion}/win32-x86.zip",
        "${emmyluaDebuggerProjectUrl }/releases/download/${emmyluaDebuggerVersion}/win32-x64.zip",
    ))

    dest("temp")
}

task("unzipDebugger", type = Copy::class) {
    dependsOn("downloadDebugger")
    from(zipTree("temp/win32-x64.zip")) {
        into("bin/win32-x64")
    }
    from(zipTree("temp/win32-x86.zip")) {
        into("bin/win32-x86")
    }
    destinationDir = file("temp")
}

task("installDebugger", type = Copy::class) {
    dependsOn("unzipDebugger")
    from("temp/bin") {
        into("bin")
    }

    destinationDir = file("src/main/resources/debugger")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildPlugin {
        dependsOn("installDebugger")
    }

    withType<org.jetbrains.intellij.tasks.PrepareSandboxTask> {
        doLast {
            copy {
                from("src/main/resources/debugger/bin")
                into("$destinationDir/${pluginName.get()}/debugger/bin")
            }
        }
    }
}
