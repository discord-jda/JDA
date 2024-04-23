rootProject.name = "JDA"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("jackson", "2.16.0")
            library("jackson-core",     "com.fasterxml.jackson.core", "jackson-core").versionRef("jackson")
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            bundle("jackson", listOf("jackson-core", "jackson-databind"))

            library("logback-classic",       "ch.qos.logback",           "logback-classic"     ).version("1.5.6")
            library("opus",                  "club.minnced",             "opus-java"           ).version("1.1.1")
            library("findbugs",              "com.google.code.findbugs", "jsr305"              ).version("3.0.2")
            library("websocket-client",      "com.neovisionaries",       "nv-websocket-client" ).version("2.14")
            library("okhttp",                "com.squareup.okhttp3",     "okhttp"              ).version("4.12.0")
            library("jna",                   "net.java.dev.jna",         "jna"                 ).version("5.14.0")
            library("trove4j",               "net.sf.trove4j",           "core"                ).version("3.1.0")
            library("commons-collections",   "org.apache.commons",       "commons-collections4").version("4.4")
            library("commons-lang3",         "org.apache.commons",       "commons-lang3"       ).version("3.14.0")
            library("assertj",               "org.assertj",              "assertj-core"        ).version("3.25.3")
            library("jetbrains-annotations", "org.jetbrains",            "annotations"         ).version("24.1.0")
            library("junit",                 "org.junit.jupiter",        "junit-jupiter"       ).version("5.10.2")
            library("mockito",               "org.mockito",              "mockito-core"        ).version("5.11.0")
            library("reflections",           "org.reflections",          "reflections"         ).version("0.10.2")
            library("slf4j",                 "org.slf4j",                "slf4j-api"           ).version("1.7.36")
        }
    }
}
