plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "JDA"

includeBuild("formatter-recipes") {
    dependencySubstitution {
        substitute(module("net.dv8tion.jda:formatter-recipes")).using(project(":"))
    }
}
