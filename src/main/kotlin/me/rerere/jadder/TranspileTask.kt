package me.rerere.jadder

import me.rerere.jadder.transformer.TransformContext
import me.rerere.jadder.transformer.TransformerRegistry
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

abstract class TranspileTask : DefaultTask() {
    @get:Input
    abstract val targetVersion: Property<BytecodeVersion>

    @get:Input
    abstract val sourceVersion: Property<BytecodeVersion>

    @TaskAction
    fun transpile() {

        println("Start transpile")
        println("Target version: ${targetVersion.get()}")

        // Scan build/classes deep
        val classes = project
            .fileTree("${project.layout.buildDirectory.asFile.get().path}/classes")

        classes.include("**/*.class")

        val transformers = TransformerRegistry.getTransformers(
            sourceVersion = sourceVersion.get(),
            targetVersion = targetVersion.get(),
        )
        transformers.forEach { transformer ->
            println("[Transformer] ${transformer::class.simpleName}")
        }

        classes.forEach { file ->
            var currentBytes  = file.readBytes()

            transformers.forEach { transformer ->
                val classReader = ClassReader(currentBytes)
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                transformer.transform(TransformContext(
                    classReader = classReader,
                    classWriter = classWriter,
                    sourceVersion = sourceVersion.get(),
                    targetVersion = targetVersion.get(),
                ))

                currentBytes = classWriter.toByteArray()
            }

            file.writeBytes(currentBytes)
            println("[Transpile] ${file.path} | ${currentBytes.size}")
        }
    }
}