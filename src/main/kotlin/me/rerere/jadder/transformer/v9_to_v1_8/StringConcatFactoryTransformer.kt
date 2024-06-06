package me.rerere.jadder.transformer.v9_to_v1_8

import me.rerere.jadder.transformer.TransformContext
import me.rerere.jadder.transformer.Transformer
import org.objectweb.asm.*

class StringConcatFactoryTransformer : Transformer {
    override fun transform(context: TransformContext) {
        context.classReader.accept(ClassTransformer(context.classWriter), 0)
    }

    class ClassTransformer(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, classVisitor) {
        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            return MethodTransformer(methodVisitor)
        }
    }

    class MethodTransformer(methodVisitor: MethodVisitor) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
        private var maxLocalIndex = 0

        override fun visitVarInsn(opcode: Int, `var`: Int) {
            super.visitVarInsn(opcode, `var`)
            if (`var` >= maxLocalIndex) {
                maxLocalIndex = `var` + 1
            }
        }

        override fun visitInvokeDynamicInsn(
            name: String?,
            descriptor: String?,
            bootstrapMethodHandle: Handle?,
            vararg bootstrapMethodArguments: Any?
        ) {
            if (name == "makeConcatWithConstants" && bootstrapMethodArguments.isNotEmpty()) {
                // 获取占位符字符串
                val constants = bootstrapMethodArguments[0] as String

                // 获取方法描述符中的参数类型
                val argTypes = Type.getArgumentTypes(descriptor)

                // 创建一个临时数组来保存栈上的参数
                val tempArrayIndex = maxLocalIndex
                maxLocalIndex += 1
                mv.visitIntInsn(Opcodes.BIPUSH, argTypes.size)
                mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
                mv.visitVarInsn(Opcodes.ASTORE, tempArrayIndex)

                // 将栈上的参数保存到临时数组中
                for (i in argTypes.indices.reversed()) {
                    mv.visitVarInsn(Opcodes.ALOAD, tempArrayIndex)
                    mv.visitIntInsn(Opcodes.BIPUSH, i)
                    mv.visitInsn(Opcodes.SWAP)
                    boxIfNeeded(argTypes[i])
                    mv.visitInsn(Opcodes.AASTORE)
                }

                // 创建 StringBuilder 实例
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

                // 从临时数组中读取参数并依次调用 StringBuilder.append
                var lastIdx = 0
                var argIndex = 0
                for (i in constants.indices) {
                    if (constants[i] == '\u0001') {
                        if (lastIdx < i) {
                            mv.visitLdcInsn(constants.substring(lastIdx, i))
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                        }
                        mv.visitVarInsn(Opcodes.ALOAD, tempArrayIndex)
                        mv.visitIntInsn(Opcodes.BIPUSH, argIndex)
                        mv.visitInsn(Opcodes.AALOAD)
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                        argIndex++
                        lastIdx = i + 1
                    }
                }
                if (lastIdx < constants.length) {
                    mv.visitLdcInsn(constants.substring(lastIdx))
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                }

                // 调用 StringBuilder.toString
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
            } else {
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
            }
        }

        private fun boxIfNeeded(type: Type) {
            when (type.sort) {
                Type.INT -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Integer",
                        "valueOf",
                        "(I)Ljava/lang/Integer;",
                        false
                    )
                }

                Type.LONG -> {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
                }

                Type.FLOAT -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Float",
                        "valueOf",
                        "(F)Ljava/lang/Float;",
                        false
                    )
                }

                Type.DOUBLE -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Double",
                        "valueOf",
                        "(D)Ljava/lang/Double;",
                        false
                    )
                }

                Type.BOOLEAN -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Boolean",
                        "valueOf",
                        "(Z)Ljava/lang/Boolean;",
                        false
                    )
                }

                Type.CHAR -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Character",
                        "valueOf",
                        "(C)Ljava/lang/Character;",
                        false
                    )
                }

                Type.SHORT -> {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Short",
                        "valueOf",
                        "(S)Ljava/lang/Short;",
                        false
                    )
                }

                Type.BYTE -> {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
                }
            }
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(0, 0)
        }
    }
}