package me.rerere.jadder.transformer

import me.rerere.jadder.BytecodeVersion
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

interface Transformer {
    fun transform(context: TransformContext)
}

class TransformContext(
    val classReader: ClassReader,
    val classWriter: ClassWriter,
    val sourceVersion: BytecodeVersion,
    val targetVersion: BytecodeVersion
)