package me.rerere.jadder

import org.gradle.api.Plugin
import org.gradle.api.Project

class JadderPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("transpileBytecode", TranspileTask::class.java)
    }
}