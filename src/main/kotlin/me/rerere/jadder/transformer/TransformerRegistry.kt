package me.rerere.jadder.transformer

import me.rerere.jadder.BytecodeVersion
import me.rerere.jadder.transformer.common.ClassFileVersionTransformer
import me.rerere.jadder.transformer.v9_to_v1_8.StringConcatFactoryTransformer

object TransformerRegistry {
    private val commonTransformers = mutableListOf<Transformer>()
    private val transformers = mutableListOf<Pair<BytecodeVersion, Transformer>>()

    private fun register(version: BytecodeVersion, transformer: Transformer) {
        transformers.add(version to transformer)
    }

    private fun registerCommon(transformer: Transformer) {
        commonTransformers.add(transformer)
    }

    // transform source version to target version
    fun getTransformers(sourceVersion: BytecodeVersion, targetVersion: BytecodeVersion): List<Transformer> {
        val sortedTransformers = transformers.sortedByDescending { it.first.version }
        val applicableTransformers = mutableListOf<Transformer>()

        applicableTransformers.addAll(commonTransformers)

        for (transformer in sortedTransformers) {
            val transformerVersion = transformer.first
            if (transformerVersion.version <= sourceVersion.version && transformerVersion.version > targetVersion.version) {
                applicableTransformers.add(transformer.second)
            }
        }

        return applicableTransformers
    }

    init {
        registerCommon(ClassFileVersionTransformer())

        register(BytecodeVersion.JVM_9, StringConcatFactoryTransformer())
    }
}