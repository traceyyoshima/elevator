plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

object Properties {
    const val ASSERT_J: String = "3.19.0"
    const val JETBRAINS_ANNOTATIONS: String = "26.0.2"
    const val JUNIT: String = "5.10.2"
    const val LOMBOK: String = "1.18.36"
    const val SLF4J: String = "2.0.17"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Properties.LOMBOK}")

    compileOnly("org.projectlombok:lombok:${Properties.LOMBOK}")

    implementation("org.jetbrains:annotations:${Properties.JETBRAINS_ANNOTATIONS}")
    implementation("org.slf4j:slf4j-api:${Properties.SLF4J}")
    implementation("org.slf4j:slf4j-simple:${Properties.SLF4J}")

    testImplementation("org.assertj:assertj-core:${Properties.ASSERT_J}")

    testImplementation(platform("org.junit:junit-bom:${Properties.JUNIT}"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}