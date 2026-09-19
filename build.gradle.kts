plugins {
    java
}

group = "io.github.lfarruda188"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Compilado contra uma API estavel; o plugin so usa metodos antigos do Bukkit,
    // por isso roda de 1.21 ate a serie 26.x sem recompilar.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// ---------------------------------------------------------------------------
// Testes da logica de ordenacao.
//
// ItemSorter e deliberadamente independente do servidor: recebe e devolve
// arrays de ItemStack. Isso permite testar a parte critica -- a que pode
// perder ou duplicar itens -- sem subir um servidor Minecraft.
// Este source set compila o ItemSorter contra stubs minimos do Bukkit, nao
// contra a paper-api, e roda os testes como um programa Java comum.
// ---------------------------------------------------------------------------
val logicTest: SourceSet by sourceSets.creating {
    java.setSrcDirs(listOf("src/logicTest/stubs", "src/logicTest/java", "src/main/java"))
    java.exclude("**/QuietSortPlugin.java", "**/SortListener.java")
    compileClasspath = files()
    runtimeClasspath = output
}

val logicTestRun by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Roda os testes da logica de ordenacao (perda/duplicacao de itens)."
    dependsOn(tasks.named("logicTestClasses"))
    classpath = logicTest.runtimeClasspath
    mainClass.set("TestSorter")
}

tasks.named("check") {
    dependsOn(logicTestRun)
}

tasks.jar {
    archiveBaseName.set("QuietSort")
    archiveClassifier.set("")
}
