package com.davidmedenjak.indiana.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.uast.UFile

class Material3ImportAliasDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf(UFile::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitFile(node: UFile) {
                val ktFile = node.sourcePsi as? KtFile ?: return

                // Phase 1: Collect unaliased M3 imports
                val unaliasedImports = mutableMapOf<String, KtImportDirective>()
                for (import in ktFile.importDirectives) {
                    val name = getUnaliasedM3Name(import) ?: continue
                    unaliasedImports[name] = import
                }
                if (unaliasedImports.isEmpty()) return

                // Phase 2: Walk PSI tree for usages
                val usages = mutableMapOf<String, MutableList<KtSimpleNameExpression>>()
                ktFile.accept(object : KtTreeVisitorVoid() {
                    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
                        super.visitSimpleNameExpression(expression)
                        // Skip references inside import directives
                        var parent = expression.parent
                        while (parent != null) {
                            if (parent is KtImportDirective) return
                            parent = parent.parent
                        }
                        val name = expression.getReferencedName()
                        if (name in unaliasedImports) {
                            usages.getOrPut(name) { mutableListOf() }.add(expression)
                        }
                    }
                })

                // Phase 3: Report on each usage with composite fix
                for ((name, refs) in usages) {
                    val import = unaliasedImports[name] ?: continue
                    val fqName = import.importedFqName?.asString() ?: continue

                    val importFix = LintFix.create()
                        .replace()
                        .range(context.getLocation(import))
                        .text("import $fqName")
                        .with("import $fqName as M3$name")
                        .build()

                    val usageFixes = refs.map { ref ->
                        LintFix.create()
                            .replace()
                            .range(context.getLocation(ref))
                            .text(name)
                            .with("M3$name")
                            .build()
                    }

                    val compositeFix = LintFix.create()
                        .name("Replace with M3$name")
                        .composite(importFix, *usageFixes.toTypedArray())

                    for (ref in refs) {
                        context.report(
                            ISSUE,
                            context.getLocation(ref),
                            "Material 3 $name should use M3$name alias",
                            compositeFix,
                        )
                    }
                }
            }
        }
    }

    private fun getUnaliasedM3Name(import: KtImportDirective): String? {
        val fqName = import.importedFqName?.asString() ?: return null

        if (!fqName.startsWith("androidx.compose.material3.")) return null

        val name = fqName.substringAfterLast(".")

        // Skip star imports
        if (name == "*") return null

        // Skip sub-package imports (e.g. material3.pulltorefresh.X)
        val afterM3 = fqName.removePrefix("androidx.compose.material3.")
        if (afterM3.contains(".")) return null

        // Skip Experimental* annotations
        if (name.startsWith("Experimental")) return null

        // Skip Local* composition locals
        if (name.startsWith("Local")) return null

        // Skip lowercase-starting names (functions like contentColorFor)
        if (name.first().isLowerCase()) return null

        // Already aliased
        if (import.alias?.name != null) return null

        return name
    }

    companion object {
        val ISSUE = Issue.create(
            id = "Material3ImportAlias",
            briefDescription = "Material 3 imports should use M3 alias",
            explanation = "All Material 3 component imports should use an `as M3<Name>` alias " +
                "to distinguish them from the project's design system wrappers.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                Material3ImportAliasDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
