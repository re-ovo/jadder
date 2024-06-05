package me.rerere.jadder

// https://javaalmanac.io/bytecode/versions/
enum class BytecodeVersion(val version: Int) {
    JVM_1_8(52),
    JVM_9(53),
    JVM_11(55),
    JVM_17(61),
    JVM_21(65);

    companion object {
        fun fromVersion(version: Int): BytecodeVersion {
            return entries.first { it.version == version }
        }
    }
}