[maven-central]: https://img.shields.io/maven-central/v/net.dv8tion/JDA-opus-jna?filter=!*-preview*&logo=apachemaven&color=blue
[jitpack]: https://img.shields.io/badge/Snapshots-JitPack?logo=jitpack
[installation]: #-installation
[license]: https://github.com/discord-jda/JDA/tree/master/LICENSE
[license-shield]: https://img.shields.io/badge/License-Apache%202.0-white.svg

<img align="right" src="https://github.com/discord-jda/JDA/blob/assets/assets/readme/logo.png?raw=true" height="150" width="150">

[![maven-central][]][installation]
[![jitpack][]](https://jitpack.io/#discord-jda/JDA)
[![license-shield][]][license]

# JDA Opus Support

This module enables JDA to encode and decode Opus audio. It is not necessary if your bot can handle Opus audio by itself.

## 🔬 Installation

[![maven-central][]](https://central.sonatype.com/artifact/net.dv8tion/JDA-opus-jna)
[![jitpack][]](https://jitpack.io/#discord-jda/JDA)

This module is available on maven central. The latest version is always shown in the [GitHub Release](https://github.com/discord-jda/JDA/releases/latest).

The minimum java version supported by this module is **Java SE 8**.

> [!NOTE]
> The Jitpack artifact must be retrieved using the `io.github.discord-jda.JDA` group ID and `opus-jna` as the artifact ID.

### Gradle

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // There is no API to use, it is detected at runtime
    runtimeOnly("net.dv8tion:JDA-opus-jna:$version")
}
```

### Maven

```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA-opus-jna</artifactId>
    <version>$version</version> <!-- replace $version with the latest version -->
    <!-- There is no API to use, it is detected at runtime -->
    <scope>runtime</scope>
</dependency>
```
