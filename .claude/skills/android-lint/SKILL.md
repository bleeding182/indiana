---
name: android-lint
description: Guide for creating, testing, and maintaining custom Android lint checks in the :lint module. Invoke when working on lint checks, lint rules, lint detectors, or lint tests.
disable-model-invocation: false
allowed-tools:
  - Bash(./gradlew :lint:test*)
  - Bash(./gradlew :theme:lintDebug*)
  - Edit
  - Write
  - Read
  - Glob
  - Grep
---

# Android Lint — Custom Checks Guide

## 1. Project Lint Module Structure

```
lint/
├── build.gradle.kts                     # Kotlin JVM module (not Android)
├── src/main/kotlin/.../lint/
│   ├── IndianaIssueRegistry.kt          # Registry — lists all issues
│   └── Material3ImportAliasDetector.kt  # Example detector
├── src/main/resources/META-INF/services/
│   └── com.android.tools.lint.client.api.IssueRegistry   # Service loader
└── src/test/kotlin/.../lint/
    └── Material3ImportAliasDetectorTest.kt
```

Consumer modules pull it in with:
```kotlin
// theme/build.gradle.kts
lintChecks(project(":lint"))
```

**Not** `implementation` or `api` — only `lintChecks`.

## 2. Step-by-step: Adding a New Lint Check

1. **Create the detector** at `lint/src/main/kotlin/com/davidmedenjak/indiana/lint/<Name>Detector.kt`
   - Extend `Detector()` and implement `Detector.UastScanner`
2. **Define `ISSUE`** in a `companion object` (see template below)
3. **Register in `IndianaIssueRegistry`** — add the issue to the `issues` list
4. **Write tests** at `lint/src/test/kotlin/com/davidmedenjak/indiana/lint/<Name>DetectorTest.kt`
5. **Run tests**: `./gradlew :lint:test`
6. **Integration check**: `./gradlew :theme:lintDebug` (or whichever module has `lintChecks(project(":lint"))`)

## 3. Detector Template

```kotlin
package com.davidmedenjak.indiana.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UFile

class ExampleDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf(UFile::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitFile(node: UFile) {
                // analysis logic here
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ExampleIssueId",           // Stable ID — used in lint.xml suppressions
            briefDescription = "Short summary",
            explanation = "Longer explanation of why this matters and how to fix it.",
            category = Category.CORRECTNESS, // or PERFORMANCE, USABILITY, etc.
            priority = 6,
            severity = Severity.WARNING,     // WARNING for style, ERROR for correctness
            implementation = Implementation(
                ExampleDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
```

### Writing Rules

- Use UAST (`UFile`, `UCallExpression`, etc.) for cross-language Java/Kotlin analysis
- Drop to Kotlin PSI (`node.sourcePsi as? KtFile`) only when UAST doesn't expose what you need (e.g. import alias names)
- Keep `getApplicableUastTypes()` narrow — only visit the node types you actually inspect
- Provide `LintFix` quickfixes for actionable issues:
  ```kotlin
  val fix = LintFix.create()
      .replace()
      .text("old text")
      .with("new text")
      .build()
  context.report(ISSUE, location, "Message without markdown backticks", fix)
  ```
- Issue `id` must be stable — consumers reference it in `lint.xml` baselines and suppressions
- Lint message text must **not** contain markdown backticks (they get stripped in text output and break test assertions)

## 4. Test Template

```kotlin
package com.davidmedenjak.indiana.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class ExampleDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = ExampleDetector()

    override fun getIssues(): List<Issue> = listOf(ExampleDetector.ISSUE)

    override fun lint(): TestLintTask = super.lint().allowMissingSdk()

    // Stubs for external types the test code imports
    private val someStub: TestFile = kotlin(
        """
        package com.example.external

        class SomeClass
        """,
    ).indented()

    fun testPositiveCase() {
        lint()
            .files(
                someStub,
                kotlin(
                    """
                    package test

                    import com.example.external.SomeClass

                    fun test() {}
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:3: Warning: ... [ExampleIssueId]
                import com.example.external.SomeClass
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    fun testNegativeCase() {
        lint()
            .files(
                someStub,
                kotlin(
                    """
                    package test

                    fun clean() {}
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testQuickfix() {
        lint()
            .files(
                someStub,
                kotlin(
                    """
                    package test

                    import com.example.external.SomeClass

                    fun test() {}
                    """,
                ).indented(),
            )
            .run()
            .expectFixDiffs(
                """
                Fix for src/test/test.kt line 3: Replace with ...:
                @@ -3 +3
                - import com.example.external.SomeClass
                + import com.example.external.SomeClass as Aliased
                """.trimIndent(),
            )
    }
}
```

### Testing Rules

- **Always call `allowMissingSdk()`** on the `lint()` task — CI and local dev may lack a full SDK
- **Stubs are mandatory** — the lint test framework requires every imported type to be resolvable. Provide minimal `TestFile` stubs with just the class/function signatures
- **Always use `.indented()`** on all test source files (stubs and test code)
- Cover **both positive (flagged) and negative (clean)** cases
- **Always test quickfix output** with `.expectFixDiffs()` if your detector provides a `LintFix`
- Test class methods must start with `test` (JUnit 3 convention used by `LintDetectorTest`)

## 5. Key Gotchas

| Topic | Detail |
|---|---|
| **Lint version formula** | Lint version = AGP version + 23. Currently AGP `9.0.1` → lint `32.0.1` |
| **Dependency scopes** | `lint-api` must be both `compileOnly` (main) AND `testImplementation` (test). `lint-checks` is `compileOnly`. `lint-tests` is `testImplementation` only |
| **Service loader** | `src/main/resources/META-INF/services/com.android.tools.lint.client.api.IssueRegistry` must contain the fully qualified registry class name. Without it, lint won't discover your checks |
| **Module type** | `:lint` is a Kotlin JVM module (`plugins { kotlin("jvm") }`), not an Android module — lint checks don't need the Android framework |
| **Consumer wiring** | Use `lintChecks(project(":lint"))` in consumer modules, never `implementation` |
| **Version catalog** | Lint dependencies are in `gradle/libs.versions.toml` under the `lint` version key |
| **No backticks in messages** | Lint strips markdown in plaintext output — backticks in issue messages break test assertions |

## 6. Verification Commands

```bash
# Run lint unit tests
./gradlew :lint:test

# Integration check on consuming module
./gradlew :theme:lintDebug
```

## 7. Reference: Existing Files

- Registry: `lint/src/main/kotlin/com/davidmedenjak/indiana/lint/IndianaIssueRegistry.kt`
- Detector example: `lint/src/main/kotlin/com/davidmedenjak/indiana/lint/Material3ImportAliasDetector.kt`
- Test example: `lint/src/test/kotlin/com/davidmedenjak/indiana/lint/Material3ImportAliasDetectorTest.kt`
- Build config: `lint/build.gradle.kts`
- Service loader: `lint/src/main/resources/META-INF/services/com.android.tools.lint.client.api.IssueRegistry`