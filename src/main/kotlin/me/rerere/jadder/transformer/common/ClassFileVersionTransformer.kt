package me.rerere.jadder.transformer.common

import me.rerere.jadder.transformer.TransformContext
import me.rerere.jadder.transformer.Transformer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ClassFileVersionTransformer : Transformer {
    override fun transform(context: TransformContext) {
        context.classReader.accept(object : ClassVisitor(Opcodes.ASM9, context.classWriter) {
            override fun visit(
                version: Int,
                access: Int,
                name: String?,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
            ) {
                super.visit(context.targetVersion.version, access, name, signature, superName, interfaces)
            }
        }, 0)
    }
}