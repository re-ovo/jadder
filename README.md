# Jadder
Convert high-version Java bytecode to equivalent low-version bytecode for compatibility

## Usage
```kotlin
plugins {
    id("me.rerere.jadder") version "1.0-SNAPSHOT"
}

tasks {
    withType<TranspileTask> {
        targetVersion.set(BytecodeVersion.JVM_1_8)
        sourceVersion.set(BytecodeVersion.JVM_21)
    }
}
```

## Transformers

- Java9 -> Java8
    - `StringConcatFactory` to `StringBuilder`