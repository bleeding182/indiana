package com.davidmedenjak.indiana.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class Material3ImportAliasDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = Material3ImportAliasDetector()

    override fun getIssues(): List<Issue> = listOf(Material3ImportAliasDetector.ISSUE)

    override fun lint(): TestLintTask = super.lint().allowMissingSdk()
        .skipTestModes(TestMode.IMPORT_ALIAS)

    private val m3Stubs: TestFile = kotlin(
        """
        package androidx.compose.material3

        fun Button(onClick: () -> Unit = {}, content: () -> Unit = {}) {}
        fun Text(text: String) {}
        fun Card(content: () -> Unit = {}) {}
        object ButtonDefaults { fun shapes(): Any = Any() }
        object MaterialTheme
        annotation class ExperimentalMaterial3Api
        object LocalContentColor
        fun contentColorFor(color: Any): Any = color
        """,
    ).indented()

    private val m3SubPackageStubs: TestFile = kotlin(
        """
        package androidx.compose.material3.pulltorefresh

        fun PullToRefreshBox(content: () -> Unit = {}) {}
        """,
    ).indented()

    fun testSingleUsage() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Button

                    fun test() {
                        Button()
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:6: Warning: Material 3 Button should use M3Button alias [Material3ImportAlias]
                    Button()
                    ~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    fun testMultipleUsagesOfSameSymbol() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Text

                    fun test() {
                        Text("hello")
                        Text("world")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:6: Warning: Material 3 Text should use M3Text alias [Material3ImportAlias]
                    Text("hello")
                    ~~~~
                src/test/test.kt:7: Warning: Material 3 Text should use M3Text alias [Material3ImportAlias]
                    Text("world")
                    ~~~~
                0 errors, 2 warnings
                """.trimIndent(),
            )
    }

    fun testPropertyAccess() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.ButtonDefaults

                    fun test() {
                        ButtonDefaults.shapes()
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:6: Warning: Material 3 ButtonDefaults should use M3ButtonDefaults alias [Material3ImportAlias]
                    ButtonDefaults.shapes()
                    ~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    fun testMultipleDifferentSymbols() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Button
                    import androidx.compose.material3.Text

                    fun test() {
                        Button()
                        Text("hello")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:7: Warning: Material 3 Button should use M3Button alias [Material3ImportAlias]
                    Button()
                    ~~~~~~
                src/test/test.kt:8: Warning: Material 3 Text should use M3Text alias [Material3ImportAlias]
                    Text("hello")
                    ~~~~
                0 errors, 2 warnings
                """.trimIndent(),
            )
    }

    fun testAliasedImportClean() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Button as M3Button

                    fun test() {
                        M3Button()
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testMixedAliasedAndBare() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Button as M3Button
                    import androidx.compose.material3.Text

                    fun test() {
                        M3Button()
                        Text("hello")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expect(
                """
                src/test/test.kt:8: Warning: Material 3 Text should use M3Text alias [Material3ImportAlias]
                    Text("hello")
                    ~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    fun testImportWithNoUsages() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Button

                    fun test() {}
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testExperimentalAnnotationClean() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.ExperimentalMaterial3Api

                    @ExperimentalMaterial3Api
                    fun test() {}
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testLocalCompositionClean() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.LocalContentColor

                    fun test() {
                        LocalContentColor
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testLowercaseFunctionClean() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.contentColorFor

                    fun test() {
                        contentColorFor("")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testSubPackageClean() {
        lint()
            .files(
                m3SubPackageStubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.pulltorefresh.PullToRefreshBox

                    fun test() {
                        PullToRefreshBox()
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testStarImportClean() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.*

                    fun test() {
                        Button()
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectClean()
    }

    fun testQuickfixTransformsEntireFile() {
        lint()
            .files(
                m3Stubs,
                kotlin(
                    """
                    package test

                    import androidx.compose.material3.Text

                    fun test() {
                        Text("hello")
                        Text("world")
                    }
                    """,
                ).indented(),
            )
            .run()
            .expectFixDiffs(
                """
                Fix for src/test/test.kt line 6: Replace with M3Text:
                @@ -3 +3 @@
                -import androidx.compose.material3.Text
                +import androidx.compose.material3.Text as M3Text
                @@ -6,2 +6,2 @@
                -    Text("hello")
                -    Text("world")
                +    M3Text("hello")
                +    M3Text("world")
                Fix for src/test/test.kt line 7: Replace with M3Text:
                @@ -3 +3 @@
                -import androidx.compose.material3.Text
                +import androidx.compose.material3.Text as M3Text
                @@ -6,2 +6,2 @@
                -    Text("hello")
                -    Text("world")
                +    M3Text("hello")
                +    M3Text("world")
                """.trimIndent(),
            )
    }
}
