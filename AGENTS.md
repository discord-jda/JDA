# AGENTS.md

Repository-specific rules for AI agents and automated contributors working in this project.
Humans should read [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md) first; the rules below are the machine-enforced subset plus the guardrails that are not obvious from the code.

This is **JDA (Java Discord API)** — a published library, not an application. Almost every rule here exists to protect the public API contract for downstream consumers.

## Build environment

Java is installed via SDKMAN (`~/.sdkman`); `source "$HOME/.sdkman/bin/sdkman-init.sh"` in any new shell to put `java`/`javac` on `PATH`. Temurin `25.0.4-tem` is the default, matching CI (`temurin`) and `jitpack.yml` (`25-tem`). `unzip` is required by SDKMAN and is not present in the base image.

Configure `-Xlint` and Error Prone settings together, then run `./gradlew check` before pushing — the environment has a working JDK, so "it compiles" is verifiable here and should never be assumed.

## Project facts

- Gradle 9.7.1 (wrapper), built and tested on a **JDK 25** toolchain.
- Published bytecode target is **JVM 25**: `javaVersion = JavaLanguageVersion.of(25)`, `options.release = 25`, and `verifyBytecodeVersion` asserts class-file major version **69**.
- ~208,000 LOC across ~1,218 Java files in `src/main`, plus ~91 test files.
- Split: `net.dv8tion.jda.api` (public, 805 files) vs `net.dv8tion.jda.internal` (405 files, excluded from Javadoc).
- Several Gradle modules: root, `buildSrc` (Kotlin, generates Java REST model sources via JavaPoet), `formatter-recipes` (Kotlin, OpenRewrite recipe), plus the `examples` source set.
- Consumer-facing docs live at <https://docs.jda.wiki> and the wiki at <https://jda.wiki>.

## Commands

| Task | Purpose |
|---|---|
| `./gradlew build` | Full build; run before opening a PR |
| `./gradlew test` | Main test suite (JUnit 5, Mockito, AssertJ, ArchUnit) |
| `./gradlew format` | Apply all formatters (Spotless + Error Prone patching + version catalog) |
| `./gradlew checkFormat` | Verify formatting without applying (`spotlessCheck` + `rewriteDryRun`) |
| `./gradlew check` | `checkFormat` + tests + `verifyBytecodeVersion` |
| `./gradlew updateTestSnapshots` | Regenerate snapshot test fixtures |
| `./gradlew generateApiModels` | Regenerate REST DTOs from the Discord OpenAPI spec |
| `./gradlew javadoc` | Build the published API docs |
| `./gradlew publishToMavenLocal` | Publish locally (used by JitPack and downstream testing) |

Always run `./gradlew format` before `./gradlew build`. A large share of CI failures in this repo are formatting or OpenRewrite discrepancies, not logic errors.

## Hard rules

### Bytecode target
Every compiled class in `main` must be **JVM 25** bytecode (major version 69). `verifyBytecodeVersion` runs automatically after `compileJava` **and** `compileKotlin` and fails the build otherwise. The minimum runtime for consumers is Java 25. Do not lower `options.release`, `javaVersion`, or the Kotlin `jvmTarget`/`jvmToolchain`. See `MIGRATION.md`.

### Warning-free compilation
`compileJava` uses `-Werror` with `-Xlint:all`. Only these are suppressed, each for a stated reason:
`-Xlint:-removal` (8 classes override `finalize()`, deprecated for removal but still supported), `-Xlint:-serial` (exceptions are not meant to be serialized), `-Xlint:-this-escape` (member calls in constructors for argument checks), `-Xlint:-try` (resources used as locks), `-Xlint:-varargs` (handled by `@SafeVarargs`).

Do not add blanket suppressions. If a new warning must be suppressed, add a narrow, commented `-Xlint` entry or fix the cause.

### Error Prone
Enabled for `main` and `test` (disabled entirely for `examples`). There is a curated `disable(...)` list in `build.gradle.kts`; do not extend it casually, and never `disableAllChecks` outside `examples`.

The list includes style checks that only become applicable at a higher language level (`PatternMatchingInstanceof`, `StatementSwitchToExpressionSwitch`, `StringConcatToTextBlock`, `UnnamedVariable`). These are syntax-conversion suggestions rather than defects, and converting the ~90 affected call sites is a deliberate follow-up, not a build fix. New code should still prefer modern syntax where it reads better.

### Nullability annotations (non-negotiable for public API)
The `net.dv8tion.jda.api` package maintains an enforced nullability contract, checked by `ArchUnitComplianceTest`:

- Every public method returning `RestAction` or `CompletableFuture` must be annotated `@CheckReturnValue` **and** `@Nonnull`.
- Every public method returning a non-primitive type must carry `@Nonnull`, `@Nullable`, `@Contract`, or `@UnknownNullability`.
- Non-primitive parameters must have a nullability annotation, except for the documented exclusions in the test.
- Methods returning primitives must **not** have nullability annotations.

Annotations are JSR-305 (`javax.annotation.Nonnull` / `javax.annotation.Nullable`, from `com.google.code.findbugs:jsr305`), **not** JetBrains annotations. `rewrite.yml` migrates stray JetBrains annotations to JSR-305, and the `MigrateToJavaxAnnotations` recipe enforces it. Use `net.dv8tion.jda.annotations.*` for project-specific annotations (`@Incubating`, `@DeprecatedSince`, `@ReplaceWith`, `@UnknownNullability`).

### API compatibility
This is a published library. Do not:
- change or reorder public method signatures, or change their nullability;
- rename public types, methods, or enum constants without `@Deprecated` + `@ReplaceWith`;
- widen or narrow visibility of public API;
- convert a public type in ways that alter its bytecode shape (see `MIGRATION.md` for the Kotlin-specific cases).

New public API requires documentation and, for features, a usage example in the PR description.

### Documentation
All public API methods and types must have Javadoc. Javadoc is validated (`Xdoclint:all,-missing`) and formatted by a custom OpenRewrite recipe, `net.dv8tion.jda.recipe.JavadocFormatter`, which `rewriteDryRun` enforces. Anything under `internal` is excluded from the published docs, so internal code does not need the same treatment.

`@Incubating` marks functionality that may change in a future release; use it for new API that is not yet stable.

### Formatting
- Palantir Java Format (2.84.0), Javadoc formatting disabled in Spotless because the OpenRewrite recipe owns it.
- Apache 2.0 license header from `gradle/copyright-header.txt` is applied to `src/**/*.java` — new files must include it.
- Import order: `""`, `java`, `javax`, `\#`.
- Line endings: CRLF for source (`.editorconfig`, `.gitattributes`); `gradlew` is LF.
- Kotlin files under `buildSrc`/`formatter-recipes` are covered by `spotlessKotlinGradle`. `src/**/*.kt` is covered by `spotlessKotlin` (ktlint) and uses the same Apache 2.0 header.
- `gradle/detekt.yml` configures Kotlin static analysis. Keep it small: disable a rule only with a stated reason, matching the `-Xlint` policy above.
- Dokka is pinned to `2.1.0`; `2.2.0` and `2.3.0-Beta` fail on JDK 25. It runs as `dokkaGenerateHtml` (V2), not `dokkaHtml` (V1, removed).

### Build configuration
- The version catalog lives at `gradle/libs.versions.toml`; use `alias(libs...)`/`libs...` rather than hardcoding versions.
- `@pin` comments in the catalog mark versions held back deliberately (e.g. JUnit 6 requires Java 17+). Do not "upgrade" a pinned entry without understanding why it is pinned.
- Dependency versions are managed by `versionCatalogUpdate` with a stable-only selector.

## Workflow rules

- **One logical change per PR.** Per `CONTRIBUTING.md`, do not bundle unrelated refactors. During the Kotlin migration this means strictly one package per PR.
- **Reference relevant issues or API docs**, especially for new Discord features — link the upstream `discord-api-docs` PR.
- **Follow JDA's existing coding style even where it is unconventional.** Consistency beats local preference here.
- Run `./gradlew format` then `./gradlew build` before finishing. Never commit with `check` failing.
- Prefer editing existing files over creating new ones; do not leave temporary or duplicate `*_test`/`*_fix`/`*_old` files behind.

## Tests

- JUnit 5 (`org.junit.jupiter`), Mockito (as a `-javaagent`), AssertJ, ArchUnit.
- `ArchUnitComplianceTest` and `ComponentConsistencyComplianceTest` enforce the API contract. If they fail after your change, the change is almost certainly wrong — do not weaken or exclude the rule to make it pass.
- Snapshot tests exist (`AbstractSnapshotTest`); regenerate with `./gradlew updateTestSnapshots` only when the output change is intended and reviewed.
- Test sources are not the place for `-Werror` exemptions; write warning-free tests.

## Security and scope

- Do not commit credentials, tokens, or `publishing-keyring.gpg`. `.gitignore` already covers `config.json`, `tokens.json`, and `publishing-keyring.gpg`.
- Do not modify `.github/workflows/publish.yml` secrets handling or `nmcp` publishing configuration without explicit human direction; publishing is a release-managed process.
- Treat `formatter-recipes` and `buildSrc` as build infrastructure: changes there affect the whole build and every contributor.

## Migration in progress

See [`MIGRATION.md`](MIGRATION.md). During the Kotlin migration, additional rules apply:

- Preserve the public ABI for Java consumers; the ABI diff gate must pass.
- Keep JSR-305 nullability annotations on the public API boundary rather than relying on Kotlin's nullability. Write `@Nonnull` **explicitly** on every public Kotlin parameter and return that needs it: a bare non-null Kotlin type emits only `org.jetbrains.annotations.NotNull`, which `ArchUnitComplianceTest` rejects because it requires `javax.annotation.*`. This was confirmed on the first converted class.
- Convert from the leaves inward, one package per PR, never the large files (`Guild`, `MessageChannel`, `Message`, `EntityBuilder`, `JDA`) as single units.
- `src/main/java` and `src/main/kotlin` coexist during the transition; do not delete Java sources for a package until its Kotlin replacement has passed the full verification suite.
- Do not convert an `enum` yet. Kotlin leaks a public, non-synthetic `kotlin.enums.EnumEntries getEntries()` that the compliance rules flag. Resolve that before touching any of the `api` enums.

### Kotlin build rules
The toolchain is wired and the first production files (`api.entities.SkuSnowflake`, then the `internal.utils` leaves) are converted. When converting:

- Kotlin compiles with `jvmTarget`/`jvmToolchain` 25 and `allWarningsAsErrors`. Warnings fail the build, same as Java.
- `-jvm-default=enable` is set deliberately. It is the Kotlin 2.2+ name for `-Xjvm-default=all-compatibility`; the old spelling is a deprecated arg the compiler rejects. It keeps interface body methods real `default` methods with `DefaultImpls` retained, so Java implementors of a converted interface are unaffected. Do not remove it or switch to `no-compatibility`.
- Former `static` interface methods need `@JvmStatic` in the `companion object`, otherwise Java call sites break. The interop gate in `src/test/kotlin/net/dv8tion/jda/test/kotlin` covers this and fails the build if it regresses.
- **`@JvmStatic` is for static methods, `@JvmField` is for static fields — they are not interchangeable.** A static field exposed with `@JvmStatic` still lands in `Companion` and disappears from the class; `@JvmField` is what keeps `Foo.BAR` visible to Java. Converted `ShutdownReason` public constants need `@JvmField`; static utility methods (`ClockProvider.getClock`, `UnionUtil.safeUnionCast`, `FutureUtil.thenApplyCancellable`, `EncodingUtil.*`, `ClassWalker.walk/range`) need `@JvmStatic`.
- A static-only class with no subclasses becomes an `object` declaration. Confirm there are no subclasses or implementers first (`grep -rn "extends X\|implements .*X" src/`); if any exist, keep it a `class` and mark it `open`.
- Kotlin does not inherit `java.util.Iterator.remove()`. A Kotlin class implementing a Java `Iterator` subinterface must declare `remove()`; throw `UnsupportedOperationException` to match Java's default.
- `finalize()` is not an `override` in Kotlin — declare it as a plain `protected fun` with the same `@Deprecated` message. Do not add or remove `finalize` implementations as part of a conversion.
- Preserve the exact overload set. Do not use `@JvmOverloads` to collapse constructors or methods; write secondary constructors / overloads so Java overload resolution and the resulting ABI match the original.
- Do not convert `src/test/java/net/dv8tion/jda/test/kotlin/JavaSeesKotlinProbe.java` to Kotlin. It is the Java half of the interop gate and only works while it stays Java.
- Kotlin sources need the same `gradle/copyright-header.txt` license header; `spotlessKotlin` (ktlint) enforces it. Note ktlint also rewrites signatures and Javadoc spacing, so run `./gradlew spotlessApply` before fighting a formatting failure by hand.
- `kotlin-stdlib` is an explicit `api` dependency. It is required at runtime now that the public API contains Kotlin: Kotlin emits `kotlin.jvm.internal.Intrinsics.checkNotNullParameter` for parameter null checks. Do not remove it, and do not rely on it arriving transitively through okhttp.
- `kotlin.stdlib.default.dependency=false` remains intentional: it suppresses the Kotlin plugin's *implicit* `implementation` edge, which is what actually changes the published POM. The explicit `api` declaration above is the reviewed replacement.

### Package scope of the gates
`net.dv8tion.jda.internal.*` is covered by **neither** the ABI baseline nor `ArchUnitComplianceTest` — both are scoped to `net.dv8tion.jda.api.**`. Do not read a green `apiCheck` as evidence about an `internal` conversion; the compiler, detekt, and the test suite are the only checks that see those files. Conversely, a conversion in `api` must leave `api/JDA.api` byte-identical.

### Verifying a conversion
- Run tests with `./gradlew test --rerun-tasks` after converting. A plain `./gradlew check` can leave `:test` `UP-TO-DATE` and silently validate against stale classes, which looks identical to a passing run.
- Test XML output is disabled in this build; the pass/fail summary is in `build/reports/tests/test/index.html` (505 tests, 0 failures as of the `internal.utils` batch).
- detekt (`gradle/detekt.yml`) is a real gate on Kotlin source, unlike the ABI gate. Fix findings on merit; suppress a rule only inline with `@Suppress("RuleName")` and a written reason. Structural rules that would require restructuring faithfully-ported control flow (`ReturnCount`, `IteratorNotThrowingNoSuchElementException`) are suppressed rather than rewritten, matching the `-Xlint` policy.
- Extracting a magic number into a named constant is preferred over suppressing `MagicNumber`.

### API compatibility gate
`apiCheck` (part of `check`) compares the public surface of `net.dv8tion.jda.api.**` against the checked-in baseline `api/JDA.api`. It fails on a removed class or member; additions pass.

- It is implemented with the JDK's `javap`, not `binary-compatibility-validator`, which cannot read JVM 25 bytecode (`Unsupported class file major version 69`). See `MIGRATION.md` Phase 1.
- An API change is intentional only when the baseline diff is. Run `./gradlew apiDump` and review the diff as part of the same commit; never regenerate the baseline to silence a failure you have not understood.
- Converting a Java class to Kotlin must not change this file. If it does, something in the signature changed and needs explaining.
- The task receives `sourceSets.main.output.classesDirs`, i.e. the Java and Kotlin output **directories**. It must not receive loose `.class` files: `javap` resolves binary names only against directories and jars, and the earlier loose-file wiring made the gate report every Kotlin class as removed. When you change this wiring, prove it still works by adding a bogus member to the baseline for a **Kotlin** class and confirming `apiCheck` fails.

Any gate that passes can be passing because it examined nothing. When you add or rewire a verification task, include a negative test against a Kotlin class, not just a Java one — the Phase 1 ABI gate reported success while silently skipping all Kotlin output.

When a rule here conflicts with a plausible shortcut, the rule wins. If a rule seems wrong, raise it rather than working around it.
