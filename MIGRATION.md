# Migrating JDA to Kotlin

Status: **Phase 1 complete. Phase 2 in progress — pilot (`SkuSnowflake`) and the `internal.utils` leaves have landed and verified.**

Work is parked on branch `kotlin-migration` (fork `astatio/JDA`), reviewed via **draft PR #1**, whose base is the throwaway branch `kotlin-migration-base` (`399755a`, the last pre-migration upstream merge) chosen only to give the diff a meaningful base. Fork `master` already contains these commits; the PR is a review surface, not a merge candidate. Resume by reading `AGENTS.md`, then converting the remaining `internal.utils` classes (`Checks`, `JDALogger`, `Helpers`, `IOUtil`, `PermissionUtil`, `SerializationUtil`, `ChannelUtil`), which are now unblocked because their dependencies converted.

This document describes an incremental, in-place migration of the JDA codebase from Java to Kotlin, while preserving the public API contract for Java consumers. It targets **JVM 25 bytecode** and the **latest stable Kotlin release**.

---

## 1. Scope of the change

Facts measured against `master` (JDA 6.7.0):

| Metric | Value |
|---|---|
| Main Java files / LOC | 1,218 files / ~208,000 LOC |
| — `net.dv8tion.jda.api` | 805 files |
| — `net.dv8tion.jda.internal` | 406 files |
| — `net.dv8tion.jda.annotations` | 6 files |
| Tests | 91 files / ~9,900 LOC (JUnit 5, Mockito, AssertJ, ArchUnit) |
| Examples / Java 8 compat suite | 5 files / 1 file |
| Public top-level types | ~1,118 (349 interfaces, 61 enums, 57 abstract classes) |
| `default` interface methods | ~1,468 |
| `static` interface methods | ~170 |
| Wildcard signatures (`? extends` / `? super`) | ~551 across 146 files |
| Generic (parameterized) types | ~420 |
| Files importing `javax.annotation` (JSR-305) | 959 |
| `@SafeVarargs` / files with varargs | 6 / 190 |
| Package-private top-level classes | 5 |

Relevant build facts:

- Gradle **9.7.1** (wrapper), builds on a **JDK 25** toolchain.
- Published bytecode target: **was** Java 8 at the time these metrics were taken (`libraryJavaVersion = 8`, `options.release = 8`, `verifyBytecodeVersion` asserting class-file major version **52**). Phase 0 has since raised this to JVM 25 — see §4.
- Publishing is Maven Central via `nmcp`, with `sources` + `javadoc` jars and four jar variants (`jar`, `shadowJar`, `noOpusJar`, `minimalJar`) plus artifact exclusion filters (opus/JNA/tink).
- Tooling is Java-only: Palantir formatter (Spotless), Error Prone (large disabled-check list), OpenRewrite recipes (`NeedBraces`, `NoFinalizedLocalVariables`, `JavadocFormatter`, `MigrateToJavaxAnnotations`) gated by `rewriteDryRun`.
- The REST model generator emits **Java** via Palantir JavaPoet into `net.dv8tion.jda.internal.generated.*Dto`, filtered by a JavaParser-based task.
- Kotlin is already declared in `gradle/libs.versions.toml` (`org.jetbrains.kotlin.jvm:2.4.20`) for `buildSrc`'s `kotlin-dsl`, but is not applied to the root project.
- `jda-ktx` is a separate downstream Kotlin library that consumes the published Java API.

---

## 2. Decisions and drivers

| Decision | Choice |
|---|---|
| Migration model | **Option C** — incremental, in-place, package by package, with Java interop |
| Bytecode target | **JVM 25** (class-file major version 69) |
| Kotlin version | **Latest stable** (2.4.x line; the version catalog already pins 2.4.20) |
| Java consumer compatibility | Preserve public **source and binary** shape for consumers running on Java 25 |
| Min runtime for consumers | **Java 25** (raised from Java 8 — a breaking change) |
| Generated DTOs | Stay Java for now (see §7) |

### 2.1 Why incremental

JDA is a library with a deliberately stable, heavily annotated public API and a large documented surface. A big-bang rewrite would put the whole API at risk in one step and make review and bisecting impossible. Converting package by package, gated by an automated ABI diff, keeps the library shippable across the whole migration.

### 2.2 Why JVM 25 changes the calculus

Raising the target to JVM 25 is itself a **breaking change**: consumers on Java 8–24 can no longer load the artifact. That change is independent of the language migration and should be shipped and announced on its own line before Kotlin conversion begins, so that any fallout is attributable to one change rather than two.

Benefits of the JVM 25 target for this migration:

- Kotlin's Java 8 support window no longer constrains the Kotlin version. We can track the latest stable Kotlin release without worrying about the compiler dropping `jvmTarget = 1.8`.
- The Java 8 compatibility source set and its hidden worst-case constraints disappear (see §6).
- Modern library APIs (`java.time`, records interop, pattern matching in any residual Java) are available to both languages.

Costs:

- Kotlin ≥ **2.3.0** is required to emit Java 25 bytecode; we will use the latest stable (2.4.x).
- Consumers must be on Java 25; this belongs in the release notes as a first-class breaking change.
- Gradle/Kotlin compatibility must be verified: the wrapper is on Gradle 9.7.1, and each Kotlin release declares a supported Gradle range. Confirm the chosen Kotlin patch supports Gradle 9.7.x, or pin the wrapper to a supported Gradle version, before starting.

---

## 3. Non-negotiable guardrails

Freeze these before Phase 1 and encode them in CI. Update `.github/CONTRIBUTING.md` and add an `AGENTS.md` capture of the build rules.

1. **Bytecode is JVM 25.** `verifyBytecodeVersion` expects major version **69**. Every compiled class, Kotlin and Java, must match. The task reads both `compileJava` and `compileKotlin` output; keep it that way, or converted classes stop being checked.
2. **Java consumers keep compiling and running on Java 25.** This requires:
   - `-jvm-default=enable` (the Kotlin 2.2+ name; formerly `-Xjvm-default=all-compatibility`) so interface `default` methods remain `default` in bytecode and Java implementors are unaffected.
   - `@JvmStatic` on every converted `companion object` member that was a `static` interface method (~170 sites).
   - `@JvmName` where Kotlin would otherwise mangle a name.
   - `@JvmOverloads` where Java had explicit overloads.
3. **Nullability stays expressed as JSR-305 annotations** (`@Nonnull`, `@Nullable`) on the public API. Do not rely on Kotlin's own nullability for the interop boundary; Kotlin's inserted `Intrinsics` checks would change runtime behavior for Java callers and break the ArchUnit contract.
4. **`@UnknownNullability` and `@Contract` must survive** as annotations because ArchUnit rules reference them explicitly.
5. **`sources` and `javadoc` jars keep being produced.** Dokka must reach parity before Javadoc is dropped.
6. **One logical change per PR**, per the existing contribution policy; here that means one package per PR.

---

## 4. Phased plan

### Phase 0 — Raise the bytecode target to JVM 25 (separate, shippable change)

**Done.** Executed in Java only, with no Kotlin applied:

- `libraryJavaVersion` / `exampleJavaVersion` collapsed into a single `javaVersion = JavaLanguageVersion.of(25)`.
- `options.release = 25` on `compileJava` (the `compileTestJava8Java` task no longer exists); the Javadoc `-release` option now uses `javaVersion`.
- `verifyBytecodeVersion` `expectedMajorVersion` changed from `52` to `69`.
- Java 8 compatibility machinery retired:
  - Removed the `testJava8` source set, `testJava8Implementation`/`testJava8RuntimeOnly` configurations, `java8Toolchain`, the `testJava8Compatibility` task, and its `check` dependency.
  - Deleted `src/test-java8` (including `MinimalJDABotTest`, whose `testCurrentJavaVersion` asserted a `1.8` runtime).
  - Removed the `junit-java8` and `junit-launcher-java8` catalog pins and the `junit-java8` bundle.
  - Removed the `-Xlint:-options` suppression, which existed only for `--release 8` notes.
- Updated `README.md`: minimum Java is now **Java 25**.
- Updated `AGENTS.md` bytecode, lint, command, and test rules to match.

CI workflows already ran JDK 25 exclusively, and `jitpack.yml` already selects `25-tem`, so no workflow changes were required.

Remaining verification for this phase: run `./gradlew build` on a JDK 25 host to confirm the full suite, `checkFormat`, and `verifyBytecodeVersion` pass with the new target. The build could not be executed in the environment where this change was authored.

**Verification now complete.** Built on Temurin 25.0.4 via SDKMAN. The first JVM 25 build failed under `-Werror` because raising the target made three Error Prone style checks newly applicable (`StatementSwitchToExpressionSwitch` ×76, `StringConcatToTextBlock` ×3, `UnnamedVariable` ×1) alongside a javac `removal` warning; these were suppressed rather than mechanically rewritten, to keep the bytecode change reviewable. `./gradlew check` then passed with **501 tests / 0 failures** and all **1,915 classes at major version 69**.

### Phase 1 — Kotlin toolchain skeleton (no files converted)

**Done.** Implemented as described below, with three deltas from the original sketch:

- The compiler flag was renamed. Kotlin 2.2 rejects `-Xjvm-default=all-compatibility` as a deprecated argument; the equivalent is now **`-jvm-default=enable`**. Confirmed against the compiler itself (`-X` help), which documents the mapping: `all-compatibility` → `enable`, `all` → `no-compatibility`, `disable` → `disable`. The semantics are unchanged, so converted interfaces still emit real `default` methods with `DefaultImpls` retained. Note there is no public typed Gradle DSL property for this in Kotlin 2.4.20 (`JvmDefaultMode` is under `internal.config`), so it remains a free compiler arg.
- `jvmToolchain(25)` is set on the `kotlin` block in addition to `jvmTarget`. Without a matching toolchain declaration, Gradle fails the build with "Inconsistent JVM Target Compatibility Between Java and Kotlin Tasks" — which is a useful second line of defense if the target is ever edited in one place only.
- `kotlin.stdlib.default.dependency=false` in `gradle.properties`. Applying the plugin would otherwise add `kotlin-stdlib` as an `implementation` dependency, changing the published POM for every downstream consumer. That is a public-ABI change, and none is warranted yet since no Kotlin type is public. Re-enable when the first Kotlin type joins the public API.

Additionally, beyond the original sketch:

- `verifyBytecodeVersion` now also reads `compileKotlin` output. It previously covered only `compileJava`, so the first converted class would have silently escaped the major-version gate. Verified by confirming the task actually enumerates Kotlin classes (not merely that it exits zero).
- `spotlessKotlin` uses `ktlint 1.6.0` and the shared `gradle/copyright-header.txt`. The header enforcement was confirmed to fail on a Kotlin file lacking it.

Apply the Kotlin plugin in `build.gradle.kts`: `alias(libs.plugins.kotlin)`.
- Add `src/main/kotlin`; the Kotlin plugin wires it into `sourceSets.main` automatically, compiling alongside `src/main/java`. (The directory is created by the first converted file; the wiring is already in place and was exercised via `src/test/kotlin`.)
- Configure the compiler:

  ```kotlin
  kotlin {
      jvmToolchain(25)

      compilerOptions {
          jvmTarget.set(JvmTarget.JVM_25)
          freeCompilerArgs.add("-jvm-default=enable") // renamed from -Xjvm-default=all-compatibility
          allWarningsAsErrors.set(true)
      }
  }
  ```

- Ensure mixed-source compilation ordering is correct (Kotlin compiles against the Java sources and the Java task sees Kotlin output) so that a Java class can reference a Kotlin class and vice versa within `main`.
- Extend Spotless with a Kotlin target (`ktlint` or `ktfmt`) that uses the existing `gradle/copyright-header.txt` license header and preserves the `GIT_ATTRIBUTES_FAST_ALLSAME` line-ending behavior. The `.editorconfig` already carries a full `[{*.gradle.kts,*.kts,*.kt}]` section to align with.
- Add `detekt` for the class of checks Error Prone provided, and add Dokka in parallel with `javadoc` rather than replacing it yet.
- Add `binary-compatibility-validator` (or `japicmp`/`revapi`) and check in a baseline from the last Phase-0 release.
- Add the migration gates to CI (§9).

Deliverable: Kotlin enabled, zero conversions, all gates green.

**Interop gate (permanent).** Rather than only asserting "Kotlin compiles", `src/test/kotlin` and `src/test/java` share one package with a Kotlin declaration that a Java test consumes. This keeps the three mechanics Phase 2 depends on load-bearing, so a regression fails a test rather than surfacing later in a converted file:

- `@JvmStatic` companion members must remain callable as `KotlinJavaInteropProbe.say(...)` from Java.
- A Kotlin interface body method must remain a real `default` method, so a Java lambda can implement the interface without overriding it.
- Kotlin must be able to call Java statics and honor their contracts (e.g. `Checks.notNull` throwing `IllegalArgumentException`).

Bytecode inspection confirms all three: the companion emits a genuine `public static String say(String)`, the interface method is `public default`, and `DefaultImpls` is retained. `./gradlew check` passes with **505 tests / 0 failures** (501 existing + 4 interop).

**Documentation.** Dokka is applied alongside javadoc, not instead of it; `javadocJar` still produces its jar, so nothing changes for consumers yet. Dokka is pinned to `2.1.0` because `2.2.0` and `2.3.0-Beta` both fail on JDK 25 (`Registry key javac.fresh.variables.for.captured.wildcards.only is not defined`, and `Missing extension point: com.intellij.java.expressionTypeNullabilityPatcher` respectively). `2.1.0` builds 20,599 HTML pages across the mixed source set. Note it exposes the V2 tasks (`dokkaGenerateHtml`/`dokkaGenerate`); the V1 `dokkaHtml` task errors out.

**Static analysis.** detekt `2.0.0-alpha.6` (stable `io.gitlab.arturbosch` line stops at `1.23.8`, an older major) covers Kotlin where Error Prone covers Java, and runs from `check`. `gradle/detekt.yml` is small and follows the same policy as the javac `-Xlint` suppressions: rules are disabled only with a stated reason (naming, since the public API keeps Java-style names; complexity thresholds, since converted types preserve their shape; comment rules, since the sources carry Javadoc). Verified that detekt fails the build on an introduced smell rather than silently passing.

**ABI gate.** `binary-compatibility-validator` cannot be used: version `0.18.2` (current stable) fails immediately with `Unsupported class file major version 69`, because its bundled ASM cannot read JVM 25 bytecode. Rather than leave the "keep the public API compatible" requirement unmet, the gate is implemented with the JDK's own `javap`, which always understands the classes the JDK produced:

- `buildSrc/.../PublicApi.kt` extracts the public surface of `net.dv8tion.jda.api.**` from bytecode and normalizes it. `Compiled from "X.java"` is dropped so converting a class to Kotlin does not read as an API change.
- `apiDump` writes the baseline to `api/JDA.api` (currently **900 classes, ~11.8k signature lines**). Review the diff before committing; a change there is a change to what Java consumers compile against.
- `apiCheck` runs from `check` and fails on a removed class or member. Additions pass, since Kotlin emits synthetic and `DefaultImpls`/`Companion` members that are not Java-visible API.

Both directions were verified rather than assumed: `apiCheck` passes against the untampered baseline, and fails with a precise message when a baseline entry has no counterpart (`member removed or changed in net.dv8tion.jda.api.entities.Message: ...`).

**Correction, found during Phase 2.** As first written, the gate was **silently blind to Kotlin output**, and Phase 1's "verified" claim above was wrong — the negative test had only ever exercised a Java class. Two independent defects:

1. `PublicApi.classNames` skipped any input that was not a directory (`if (!root.isDirectory) continue`). A `classes.from(compileKotlin.outputs.files)` FileTree resolves to loose `.class` **files**, not a directory, so every Kotlin class was dropped from the comparison.
2. Even with enumeration fixed, `javap` resolves a binary name only against **directory or jar** classpath entries. Passing loose `.class` files as the `-classpath` produced `class removed: ...SkuSnowflake`, because javap could not resolve it. The Java output was unaffected only because it happened to be handed over as a directory.

The fix is to feed both tasks the source set's **directory** outputs (`sourceSets.main.output.classesDirs`, i.e. `build/classes/java/main` + `build/classes/kotlin/main`) rather than a filtered file tree, and to teach `classNames` to accept loose class files as well as directories. `kotlinClasses` (the loose-file tree) is retained only because `verifyBytecodeVersion` legitimately wants per-file inputs and iterates files directly.

This is the failure mode to watch for on every future gate: a check that passes because it silently examined nothing. Negative tests must target a **Kotlin** class now that Kotlin output exists. Re-verified afterwards by injecting a bogus member into the baseline for the converted class and confirming a precise failure, and by confirming `apiCheck` then passes once restored.

Caveats worth knowing: `javap -public` reports `public`/`protected` members, so package-private and `internal` changes are out of scope; and generated/rewritten signatures are compared as-is, so a genuinely intentional API break needs `apiDump` plus an intentional, reviewed baseline diff.

**Phase 1 complete.** Kotlin enabled, zero production files converted, every gate runs from `./gradlew check`, and 505 tests pass / 0 failures.

### Phase 2 — Pilot conversion (`SkuSnowflake`)

The first production file, `net.dv8tion.jda.api.entities.SkuSnowflake`, was converted as a deliberate pilot to force every Phase-2 mechanic to prove itself before any further files move. It is an interface with a companion holding two `@JvmStatic` factories — the minimal shape that still exercises static method emission, nullability annotations, and `Companion` synthesis.

What the pilot established, each verified against real bytecode rather than assumed:

- **`@JvmStatic` factories keep static ABI.** Both `static SkuSnowflake fromId(long)` and `static SkuSnowflake fromId(String)` are present in the compiled interface, matching the baseline. A `public static final Companion` field and a `SkuSnowflake$Companion` class are added; both are permitted additions under the gate's additions-pass policy.
- **Nullability must be written explicitly.** A bare non-null Kotlin parameter emits only `org.jetbrains.annotations.NotNull`, which the compliance rules reject: they require `javax.annotation.*` on public parameters. Writing `@Nonnull` explicitly preserves the `RuntimeVisibleAnnotations` entry. This is the single most important mechanical rule for converted files, and is why the annotation is written out in `SkuSnowflake.kt` despite looking redundant.
- **`check` is green** with the converted class: 505 tests / 0 failures, all 8 ArchUnit compliance rules, `apiCheck`, `apiDump` parity, `detekt`, `spotlessKotlinCheck`, and `verifyBytecodeVersion`. The converted class is class-file major version **69**.
- **Both remaining gates were shown to actually see Kotlin output**, by negative test: removing `@Nonnull` from the converted method fails `testMethodsThatAcceptObjectShouldHaveNullabilityAnnotations()`, and injecting a bogus member into the baseline for the converted class fails `apiCheck` with `member removed or changed in ...SkuSnowflake`. Without that check, "check passes" would have been equally consistent with the gates examining nothing.
- **Packaging is unaffected.** The converted class appears in the main jar as `SkuSnowflake.class` + `SkuSnowflake$Companion.class`, `SkuSnowflake.kt` appears in the sources jar, and the javadoc jar still renders the class with its factory docs.

**New runtime dependency: `kotlin-stdlib`.** The pilot's most consequential discovery. Kotlin emits `kotlin/jvm/internal/Intrinsics.checkNotNullParameter` for parameter null checks, so the converted companion references stdlib from real code, not just metadata. Inspection confirmed the reference:

```
invokestatic  // Method kotlin/jvm/internal/Intrinsics.checkNotNullParameter:(Ljava/lang/Object;Ljava/lang/String;)V
```

`kotlin-stdlib` was *already* on the runtime classpath, but only **transitively through okhttp** — an accident of an unrelated dependency that could disappear on any okhttp upgrade, taking JDA's runtime with it. It is now declared explicitly as `api(libs.kotlin.stdlib)`. The `kotlin.stdlib.default.dependency=false` flag in `gradle.properties` remains, because it suppresses the plugin's *implicit* `implementation` edge; the explicit declaration is the reviewed, published replacement. This is the first published-POM change of the migration and belongs in the release notes.

Enum conversions remain blocked. Converting an enum leaks a public, non-synthetic `kotlin.enums.EnumEntries getEntries()` that the compliance rules would flag; six enums exist in `api` and none should be attempted until that is resolved (§10).

### Phase 2 — First leaf package (`internal.utils`)

With the pilot's mechanics proven, the first real batch was the 13 self-contained leaves of `net.dv8tion.jda.internal.utils`, walked leaves-inward so nothing depends on an unconverted sibling: `CacheConsumer`, `UnlockHook`, `ShutdownReason`, `ClockProvider`, `UnionUtil`, `FutureUtil`, `ResizingByteBuffer`, `EncodingUtil`, `EntityString`, `ClassWalker`, `ChainedClosableIterator`, `FallbackLogger`, `ContextRunnable`. Cases with in-package dependencies (`Checks`, `JDALogger`, `Helpers`, `IOUtil`, `PermissionUtil`, `SerializationUtil`, `ChannelUtil`) were deferred until those dependencies converted.

This package sits **outside** both gates: the ABI baseline (`api/JDA.api`) and the ArchUnit compliance rules only cover `net.dv8tion.jda.api.*`. That makes it the right place to establish conventions, but it also means the compiler and the test suite are the entire safety net — `apiCheck` passing here proves nothing about these files. Verification was therefore a full `test --rerun-tasks` (an ordinary `check` left `:test` UP-TO-DATE and would have tested stale classes), giving 505 tests / 0 failures, including `EntityStringTest`, which exercises the converted `EntityString` directly.

Conventions this batch fixed, each forced by a real failure:

- **`@JvmField` for static fields, `@JvmStatic` for static methods.** These are different mechanisms. `ShutdownReason.USER_SHUTDOWN` and friends are fields and need `@JvmField`; `ClockProvider.getClock`, `UnionUtil.safeUnionCast`, `FutureUtil.thenApplyCancellable`, the `EncodingUtil` helpers, and `ClassWalker.walk`/`range` are methods and need `@JvmStatic`. Using the wrong one either hides the member from Java or synthesizes into `Companion` and breaks every Java caller.
- **Objects replace static-only classes.** `ClockProvider`, `UnionUtil`, `FutureUtil`, and `EncodingUtil` were all static-only, and all subclassed from nowhere, so they became `object` declarations. Confirmed first with a subclass/implementer sweep across `src/` — zero hits for every class in the batch.
- **`open` only where inheritance exists.** `ShutdownReason` is kept `open` because its constructor is `public` and its field was `protected` in Java (widened to `val`, which is a superset); no other class needed `open`.
- **`java.util.Iterator.remove` must be overridden.** `ChainedClosableIterator` implements `ClosableIterator`, which extends `Iterator`. Java inherits the default `remove()`; Kotlin's `Iterator` does not, so the compiler demanded a declaration. It throws `UnsupportedOperationException`, matching Java's default.
- **`finalize()` is not an override in Kotlin.** `ChainedClosableIterator.finalize()` is declared as a plain `protected fun` carrying the same `@Deprecated` message. It is never invoked in tests, and `Object.finalize` is still present in JDK 25 (`javap` confirms), so the declaration remains valid; its removal is a separate decision from this migration.
- **Explicit typed constructor overloads, not `@JvmOverloads`.** `ContextRunnable` and `EntityString` keep their exact Java constructor and method sets as secondary constructors / overloaded `setType`, because overload resolution and the resulting ABI must match the original.
- **`@Nonnull` at the boundary.** Written explicitly even on internal code, so the annotation survives as `RuntimeVisibleAnnotations` and the files stay consistent with the pilot rule.

detekt is a source-level gate here (unlike the ABI gate, which is blind to `internal`), and it surfaced four findings, handled on merit rather than blanket-suppressed: the two magic numbers (`1.25` buffer growth, radix `16`) became named constants; `ReturnCount` on `hasNext` and `IteratorNotThrowingNoSuchElementException` on `ClassWalker.next` are suppressed with a written reason, because both flag control flow faithfully preserved from the Java original (`ClassWalker.next` does throw — `removeFirst()` on an empty deque raises `NoSuchElementException`; detekt cannot see through the deque).

#### Conversion order

Order by dependency depth, not by importance:

1. `net.dv8tion.jda.annotations` (6 files — pure annotations, trivial warm-up).
2. `net.dv8tion.jda.internal.utils` (`Checks`, `JDALogger`, `EntityString`, `PermissionUtil`, `SerializationUtil`, compressors) — exercises reflection, statics, and generics on internal code where ABI risk is lowest.
3. Remaining `internal.*` packages: `requests`, `entities`, `hooks`, `managers`, `audio`, `handle`, `binary`.
4. `api.utils`, `api.requests`, `api.managers`.
5. `api.entities`, `api.events`, `api.components`, `api.interactions`, `api.audit`, `api.modals`, `api.audio`, `api.sharding`.

A package is "done" only when the ABI diff against the baseline is empty, or every residual difference is recorded in a reviewed, checked-in allowlist file with a rationale.

Do **not** treat the largest files as single units. Each of these is its own review cycle:

- `api/entities/Guild.java` (~6,800 LOC)
- `api/entities/channel/middleman/MessageChannel.java` (~3,800)
- `api/entities/Message.java` (~3,300)
- `internal/entities/EntityBuilder.java` (~2,800)
- `internal/entities/GuildImpl.java` (~2,500)
- `api/sharding/DefaultShardManagerBuilder.java` (~2,300)
- `api/JDA.java` (~2,200)
- `api/JDABuilder.java` (~1,800)

### Phase 3 — Tests (overlaps Phase 2)

Keep the safety net in Java as long as possible.

- Leave `ArchUnitComplianceTest`, `ComponentConsistencyComplianceTest`, and `SourceSets` in Java until the very end. ArchUnit operates on bytecode, so it keeps working across the transition, and its annotation rules are precisely the contract that Kotlin can silently break. Ensure `ClassFileImporter().importPackages("net.dv8tion.jda.api")` picks up Kotlin output as well as Java.
- Convert ordinary test files opportunistically, after the production package they cover.
- Convert or delete anything that no longer applies once `src/test-java8` is gone.
- Add a Java-only interop smoke test (§8) that is *never* converted — it is the standing proof that Java call sites still work.

### Phase 4 — Publishing, docs, CI

- Generate Dokka docs covering both Java and Kotlin sources. Either emit Dokka into the existing `build/docs/javadoc` path consumed by `docs.yml`, or update the workflow and the published `javadoc` jar. Do not remove Javadoc until Dokka covers the full public surface.
- Update `maven-publish`: the `sources` jar must include Kotlin files; add `kotlin-stdlib` as an `api` dependency once a Kotlin type appears in the public API.
- Re-validate all four jar variants with Kotlin present. Kotlin emits `@Metadata` and synthetic classes; confirm `minimalJar` minimization and `duplicatesStrategy = FAIL` still behave, and refresh artifact filters if Kotlin introduces packages.
- Re-verify `verifyBytecodeVersion` against Kotlin output (Kotlin must be told `jvmTarget = 25` and must not emit anything else).

### Phase 5 — Cleanup

- Remove `src/main/java` once empty; remove Java-only tooling (OpenRewrite Java recipes, the Error Prone configuration, the Palantir Java formatter block) once no Java remains.
- Freeze the accumulated ABI baseline as the new published contract.
- Decide `MigrateToJavaxAnnotations`' fate: if the public API standardizes on Kotlin/JetBrains nullability annotations after a major release, that recipe and the JSR-305 rule in §3.3 can be revisited deliberately, not accidentally.

---

## 5. Java → Kotlin idiom mapping

| Java idiom | Kotlin approach | ABI caveat |
|---|---|---|
| Interface `default` methods (~1,468) | Default implementations in interfaces | Needs `-jvm-default=enable` (formerly `-Xjvm-default=all-compatibility`) to keep `default` in bytecode for Java implementors |
| Static interface methods (~170) | `companion object` + `@JvmStatic` | Kotlin has no true interface statics; verify with the ABI diff |
| `@Nonnull` / `@Nullable` (959 files) | Keep the JSR-305 annotations; do not use Kotlin types at the boundary | Writing the annotation explicitly preserves `javax.annotation.*` in bytecode. Note that Kotlin still emits `Intrinsics.checkNotNullParameter` even when `@Nonnull` is present (verified in the pilot), so `kotlin-stdlib` becomes a runtime dependency regardless |
| `@UnknownNullability`, `@Contract` (13 sites) | Not expressible in Kotlin; retain as annotations | Referenced by ArchUnit rules |
| Wildcards `? extends` / `? super` (~551) | `out` / `in` variance | Some parameter wildcards are unrepresentable; use `@JvmSuppressWildcards` or explicit projections and document each exception |
| `public enum` with state (61) | `enum class` | `values()`/`valueOf()` placement in bytecode differs; Java call sites are fine, bytecode reflection may not be. Confirmed: Kotlin additionally leaks a public, non-synthetic `kotlin.enums.EnumEntries getEntries()`, which the compliance rules flag — blocked until resolved |
| Package-private top-level classes (5) | `internal` | `internal` is module-scoped and mangles function names; audit for same-package Java access before converting |
| Varargs (190 files, 6 `@SafeVarargs`) | `vararg` + `@SafeVarargs` | Generic varargs need `Array<out T>` / `@JvmSuppressWildcards` |
| Checked exceptions (`InterruptedException`, `JsonProcessingException`, `DataFormatException`, `GeneralSecurityException`, …) | Kotlin has none; add `@Throws` where Java callers must catch | Omitting `@Throws` silently changes the compiled signature |
| Static nested classes | Plain nested classes (static by default) | Behavioral match; use `inner` only if the Java type was a non-static inner class |
| `final` classes (17) | Kotlin classes are final by default | Good alignment; add `open` only where subclasses actually exist |
| Anonymous classes / SAM lambdas | Object expressions / SAM conversion | Compile-only; re-check Jackson and reflection paths |
| Reflection (`AnnotatedEventManager.getDeclaredMethods`, `JDALogger` `Class.forName`) | Works, but Kotlin emits synthetic/bridge methods | Filter synthetics in `AnnotatedEventManager`; verify the slf4j provider probe and `FallbackLogger` still resolve |
| Jackson (de)serialization | Keep Jackson; add `jackson-module-kotlin` only if Kotlin classes are serialized directly | New dependency only if actually needed |

Two runtime traps to test explicitly, because no static checker will catch them:

1. Kotlin's inserted null checks on non-null parameters.
2. Kotlin's `Intrinsics` checks on platform types.

Both can turn previously legal Java calls into `NullPointerException`s.

---

## 6. What the JVM 25 target removes from the work

Retiring Java 8 simplifies the migration materially:

- **No Java 8 API ceiling.** Kotlin may use `java.time` and other post-8 JDK APIs without the old `-release 8` restrictions.
- **No `src/test-java8` suite** to keep alive or port; the `MinimalJDABotTest` run-on-JDK-8 job goes away with Phase 0.
- **No `-Xlint:-options` / `-Xlint:-try` class of suppressions** carried over from the Java 8 era.
- **No Kotlin-version ceiling** imposed by upstream JVM-1.8 target deprecation. This is the main reason the migration is now tractable: the compiler and the bytecode target are no longer fighting each other.

---

## 7. Generated REST models

`buildSrc` generates `*Dto` sources with Palantir **JavaPoet**, and a **JavaParser** task filters them; the generator is already written in Kotlin but emits Java, and the output is added directly to `sourceSets.main`.

**Recommendation: keep generating Java.** Generated Java and hand-written Kotlin coexist in the same source set with no interop cost, and this avoids touching the codegen pipeline during the risky part of the migration.

Port to KotlinPoet only if a fully Java-free tree is a hard requirement after Phase 5. If that happens, it is a self-contained follow-up: swap the `JavaFile`/`TypeSpec` construction and decide whether the JavaParser filter stays (it can — it parses generated `.java` regardless of who wrote it).

---

## 8. Testing and verification strategy

Layered gates; each package must clear all of them before merge.

1. **ABI diff** (`binary-compatibility-validator`, or `japicmp`/`revapi`) against the last Phase-0 release. This is the single most important gate. Treat the baseline as an allowlist: any diff fails CI unless explicitly added and reviewed.
2. **Bytecode version** — `verifyBytecodeVersion` at major version 69, covering Kotlin and Java output.
3. **ArchUnit compliance** — unchanged; becomes the primary detector of annotations dropped during conversion.
4. **Behavioral suite** — the 91 existing test files. Watch `JDABuilderTest`, the event/socket handler tests, `DataObjectTest`/`JsonTest` (serialization), `PermissionUtilTest`, and `CryptoAdapterTest` especially.
5. **Java interop smoke test** — a small, permanently-Java module (reuse `src/examples`) compiled and run against the published artifact after every phase. This catches `@JvmStatic`, `@JvmName`, and default-method regressions that unit tests miss.
6. **Downstream canary** — build `jda-ktx` against the migrated artifact in CI. It is a real Kotlin consumer and will surface variance and `@JvmSuppressWildcards` mistakes.

---

## 9. CI changes

Extend `.github/workflows/validate.yml` (or add `migration.yml`):

- Kotlin compile + extended `checkFormat` (Spotless/Kotlin + detekt).
- ABI diff against the last Phase-0 tag.
- Dokka build, plus a diff of the public symbol index against the Javadoc index.
- jda-ktx downstream build (token/cache permitting, or nightly).

Keep `artifacts.yml`, `publish.yml`, `dependency_submission.yml`, and `docs.yml` behaviorally identical until the Phase 4 changes are deliberate.

---

## 10. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| JVM 25 minimum breaks consumers | Certain | High | Ship in Phase 0 as its own announced breaking change; document prominently |
| Kotlin nullability changes Java runtime behavior | High | High | Keep JSR-305 annotations at the boundary; add NPE regression tests. **Confirmed in pilot**: a bare non-null parameter emits only `org.jetbrains.annotations.NotNull`, so `@Nonnull` must be written explicitly or the compliance rules fail |
| Wildcard/variance mismatches (~551 sites) | High | Medium | Convert leaves first; per-package ABI diff; document each exception |
| Static interface methods lose their static-ness (~170) | Medium | High | `@JvmStatic` in companions; ABI diff verifies. **Confirmed in pilot**: both `fromId` overloads kept their static form |
| `default` methods stop being `default` | Medium | High | `-jvm-default=enable` (renamed from `-Xjvm-default=all-compatibility`); Java-implements-interface test |
| `kotlin-stdlib` enters every consumer's classpath | Certain | Medium | Release-note callout; declared `api` once a Kotlin type is public. **Occurred in pilot** — see Phase 2: stdlib is now a hard runtime dependency via `Intrinsics.checkNotNullParameter` |
| ABI gate silently examines nothing | Medium | High | **Occurred in Phase 1** — the gate dropped all Kotlin output. Negative tests must target a Kotlin class, not a Java one |
| Converting an enum leaks `EnumEntries getEntries()` | High | Medium | Blocked: `getEntries()` is public and non-synthetic, and the compliance rules flag it. Resolve before converting any of the six `api` enums |
| Kotlin `Companion` field on a public interface | Low | Low | Additions-pass policy covers it; the field is initialized in `<clinit>` and confirmed resolvable |
| Javadoc site regression | High if Phase 4 rushed | Medium | Dokka parity gate before removing Javadoc |
| Gradle 9.7.1 vs Kotlin plugin support window | Medium | Medium | Verify the chosen Kotlin patch's supported Gradle range up front; pin the wrapper if needed |
| Shadow/minimal jar minimization breaks | Medium | Medium | Run all four jar tasks in CI every phase |
| Loss of Error Prone/OpenRewrite coverage | Certain | Low | Mirror rules in detekt; accept and document the gap |
| Reviewer fatigue and regression erosion | Medium | High | Enforce one package per PR |

---

## 11. Effort estimate

At roughly 300–500 LOC/day of review-quality conversion including tests and ABI fixing:

| Phase | Work |
|---|---|
| Phase 0 — JVM 25 target | 1–2 weeks |
| Phase 1 — Kotlin toolchain | 1–2 weeks |
| Phase 2 — Conversion | 8–14 months at one engineer; ~3–5 months with 3–4 engineers on non-overlapping packages |
| Phase 3 — Tests | 1–2 months (overlaps Phase 2) |
| Phase 4 — Publishing/docs/CI | 1–2 months |
| Phase 5 — Cleanup | 2–3 weeks |

**Total: roughly 6–12 engineer-months** to the last deleted Java file, spread across multiple releases, plus permanent maintenance overhead (Dokka, detekt, Kotlin version tracking, `kotlin-stdlib` on the consumer classpath).

Phase 0 is independently valuable and cheap; it should be treated as a deliverable regardless of what happens after it.

---

## 12. Open questions

1. Which release line carries the Java 25 minimum, and how much notice do consumers get?
2. ~~Is the version catalog's `2.4.20` the intended Kotlin target...~~ **Resolved.** `2.4.20` is a real, published Kotlin release and compiles the project cleanly on JDK 25 with Gradle 9.7.1, so it is the intended target. Nothing blocks on a downgrade to `2.4.10`. The catalog pin is shared with `formatter-recipes`, so moving it moves both.
3. Is a Kotlin-first companion module (rather than only upward conversion) wanted, to deliver value before the internals are converted?
4. Is dropping the Javadoc site for Dokka acceptable, and on what timeline?
5. Do generated DTOs stay Java indefinitely, or is KotlinPoet a stated end goal?
6. After the internals are Kotlin, does the public API keep JSR-305 nullability annotations (best interop) or migrate to Kotlin-native annotations in a future major (cleaner Kotlin, worse Java interop)?
