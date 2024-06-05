package me.rerere.jadder.transformer.v9_to_v1_8

import me.rerere.jadder.transformer.TransformContext
import me.rerere.jadder.transformer.Transformer
import org.objectweb.asm.*

class StringConcatFactoryTransformer : Transformer {
    override fun transform(context: TransformContext) {
        context.classReader.accept(object : ClassVisitor(Opcodes.ASM9, context.classWriter) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                return object :
                    MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                        super.visitMaxs(0, 0)
                    }

                    override fun visitInvokeDynamicInsn(
                        name: String?,
                        descriptor: String?,
                        bootstrapMethodHandle: Handle?,
                        vararg bootstrapMethodArguments: Any?
                    ) {
                        if (name == "makeConcatWithConstants" && bootstrapMethodHandle?.owner == "java/lang/invoke/StringConcatFactory") {
                            val constantString = bootstrapMethodArguments[0] as String
                            val args = Type.getArgumentTypes(descriptor)
                            val returnType = Type.getReturnType(descriptor)

                            // 将 `invokedynamic` 替换为 `StringBuilder` 调用
                            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
                            mv.visitInsn(Opcodes.DUP)
                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

                            var argIndex = 0
                            for (ch in constantString.toCharArray()) {
                                if (ch == '\u0001') {
                                    mv.visitVarInsn(args[argIndex].getOpcode(Opcodes.ILOAD), argIndex)
                                    mv.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        "(${args[argIndex].descriptor})Ljava/lang/StringBuilder;",
                                        false
                                    )
                                    argIndex++
                                } else {
                                    mv.visitLdcInsn(ch.toString())
                                    mv.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                        false
                                    )
                                }
                            }

                            super.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "toString",
                                "()Ljava/lang/String;",
                                false
                            )
                        } else {
                            super.visitInvokeDynamicInsn(
                                name,
                                descriptor,
                                bootstrapMethodHandle,
                                *bootstrapMethodArguments
                            )
                        }
                    }
                }
            }
        }, 0)
    }
}