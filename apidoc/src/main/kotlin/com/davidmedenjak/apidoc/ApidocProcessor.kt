package com.davidmedenjak.apidoc

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import java.io.File

class ApidocProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        invoked = true

        val options = environment.options
        val outputDir = options["apidoc.outputDir"] ?: run {
            environment.logger.error("apidoc.outputDir not set")
            return emptyList()
        }
        val moduleRoot = options["apidoc.moduleRoot"] ?: run {
            environment.logger.error("apidoc.moduleRoot not set")
            return emptyList()
        }

        val allFiles = resolver.getAllFiles().toList()
        if (allFiles.isEmpty()) return emptyList()

        // Register aggregating dependency so KSP always provides all files on re-run
        environment.codeGenerator.createNewFileByPath(
            Dependencies(true, *allFiles.toTypedArray()),
            "apidoc_marker",
            "txt",
        ).use { it.write("marker".toByteArray()) }

        val moduleName = File(moduleRoot).name
        val srcRoots = listOf(
            "$moduleRoot/src/main/java/",
            "$moduleRoot/src/main/kotlin/",
        )

        data class FileEntry(
            val packageName: String,
            val fileName: String,
            val projectPath: String,
            val content: String,
        )

        val entries = mutableListOf<FileEntry>()

        for (file in allFiles) {
            val filePath = file.filePath
            val srcRoot = srcRoots.firstOrNull { filePath.startsWith(it) } ?: continue
            val projectPath = moduleName + filePath.removePrefix(moduleRoot)
            val content = processFile(file)
            if (content.isNotBlank()) {
                entries.add(
                    FileEntry(
                        packageName = file.packageName.asString(),
                        fileName = file.fileName,
                        projectPath = projectPath,
                        content = content,
                    )
                )
            }
        }

        if (entries.isEmpty()) return emptyList()

        val grouped = entries
            .sortedBy { it.projectPath }
            .groupBy { it.packageName }
            .toSortedMap()

        val output = buildString {
            appendLine("# Theme API Catalog")
            appendLine("Auto-generated. Do not edit.")

            for ((packageName, files) in grouped) {
                val shortName = packageName.substringAfterLast('.')
                appendLine()
                appendLine("## $shortName — $packageName")

                for (file in files) {
                    appendLine()
                    appendLine("### ${file.fileName}")
                    appendLine("path: ${file.projectPath}")
                    appendLine()
                    append(file.content)
                }
            }
        }

        File(outputDir).mkdirs()
        File(outputDir, "api.md").writeText(output)

        return emptyList()
    }

    private fun processFile(file: KSFile): String = buildString {
        val declarations = file.declarations.toList()
        // Classes/interfaces/objects first, then top-level functions/properties/typealiases
        val (classes, rest) = declarations.partition { it is KSClassDeclaration }
        for (declaration in classes + rest) {
            processDeclaration(declaration, this, indent = "")
        }
    }

    private fun processDeclaration(
        declaration: KSDeclaration,
        sb: StringBuilder,
        indent: String,
    ) {
        if (!isPublic(declaration)) return
        if (isPreview(declaration)) return

        when (declaration) {
            is KSTypeAlias -> {
                val name = declaration.simpleName.asString()
                val target = renderType(declaration.type.resolve())
                sb.appendLine("${indent}typealias $name = $target")
                appendUsage(declaration, sb, indent)
            }

            is KSClassDeclaration -> {
                val kind = when (declaration.classKind) {
                    ClassKind.INTERFACE -> "interface"
                    ClassKind.CLASS -> "class"
                    ClassKind.OBJECT -> "object"
                    ClassKind.ENUM_CLASS -> "enum class"
                    ClassKind.ANNOTATION_CLASS -> return
                    else -> return
                }
                val name = declaration.simpleName.asString()
                sb.appendLine("$indent$kind $name")
                appendUsage(declaration, sb, indent)
                for (member in declaration.declarations) {
                    processDeclaration(member, sb, indent = "$indent  ")
                }
            }

            is KSFunctionDeclaration -> renderFunction(declaration, sb, indent)

            is KSPropertyDeclaration -> {
                val name = declaration.simpleName.asString()
                val type = renderType(declaration.type.resolve())
                val receiver = declaration.extensionReceiver?.resolve()?.let { renderType(it) }
                val prefix = if (receiver != null) "$receiver." else ""
                val mutable = if (Modifier.LATEINIT in declaration.modifiers ||
                    declaration.setter != null
                ) "var" else "val"
                sb.appendLine("$indent$mutable ${prefix}$name: $type")
                appendUsage(declaration, sb, indent)
            }
        }
    }

    private fun renderFunction(
        function: KSFunctionDeclaration,
        sb: StringBuilder,
        indent: String,
    ) {
        val name = function.simpleName.asString()
        if (name == "<init>") return
        // Skip synthetic data class methods
        if (indent.isNotEmpty() && isSyntheticMethod(name)) return

        val isComposable = function.annotations.any { it.shortName.asString() == "Composable" }
        val receiver = function.extensionReceiver?.resolve()?.let { renderType(it) }
        val prefix = if (receiver != null) "$receiver." else ""

        val composable = if (isComposable) "@Composable " else ""

        val params = function.parameters.joinToString(", ") { p ->
            val pName = p.name?.asString() ?: "_"
            val type = renderType(p.type.resolve())
            if (p.hasDefault) "$pName: $type = \u2026" else "$pName: $type"
        }

        val typeParams = function.typeParameters
        val typeParamsStr = if (typeParams.isNotEmpty()) {
            "<${typeParams.joinToString(", ") { it.simpleName.asString() }}> "
        } else ""

        val ret = function.returnType?.resolve()?.let { renderType(it) }
        val retStr = if (ret != null && ret != "Unit") ": $ret" else ""

        sb.appendLine("$indent${composable}fun ${typeParamsStr}${prefix}${name}($params)$retStr")
        appendUsage(function, sb, indent)
    }

    private fun appendUsage(declaration: KSDeclaration, sb: StringBuilder, indent: String) {
        val usage = declaration.annotations.firstOrNull { it.shortName.asString() == "Usage" }
        if (usage != null) {
            val desc = usage.arguments.firstOrNull { it.name?.asString() == "description" }?.value as? String
            if (desc != null) sb.appendLine("$indent  > $desc")
        }
    }

    private fun isPublic(declaration: KSDeclaration): Boolean {
        val m = declaration.modifiers
        return Modifier.PRIVATE !in m && Modifier.INTERNAL !in m && Modifier.PROTECTED !in m
    }

    private fun isSyntheticMethod(name: String): Boolean {
        return name.matches(Regex("component\\d+")) ||
            name == "copy" ||
            name == "equals" ||
            name == "hashCode" ||
            name == "toString"
    }

    private fun isPreview(declaration: KSDeclaration): Boolean {
        return declaration.annotations.any {
            it.shortName.asString() in setOf("Preview", "PreviewLightDark")
        }
    }

    private fun renderType(type: KSType): String {
        if (type.isError) return type.declaration.simpleName.asString()

        val decl = type.declaration
        val qn = decl.qualifiedName?.asString() ?: return decl.simpleName.asString()

        // Function types
        if (qn.matches(Regex("kotlin\\.Function\\d+")) ||
            qn.matches(Regex("kotlin\\.coroutines\\.SuspendFunction\\d+"))
        ) {
            val rendered = renderFunctionType(type, isSuspend = "Suspend" in qn)
            return if (type.isMarkedNullable) "($rendered)?" else rendered
        }

        val simple = decl.simpleName.asString()
        val args = type.arguments
        val base = if (args.isNotEmpty()) {
            val argsStr = args.joinToString(", ") { arg ->
                when (arg.variance) {
                    Variance.STAR -> "*"
                    Variance.COVARIANT -> "out ${renderType(arg.type!!.resolve())}"
                    Variance.CONTRAVARIANT -> "in ${renderType(arg.type!!.resolve())}"
                    else -> renderType(arg.type!!.resolve())
                }
            }
            "$simple<$argsStr>"
        } else simple

        return if (type.isMarkedNullable) "$base?" else base
    }

    private fun renderFunctionType(type: KSType, isSuspend: Boolean): String {
        val annotations = type.annotations.toList()
        val isComposable = annotations.any { it.shortName.asString() == "Composable" }
        val isExtension = annotations.any { it.shortName.asString() == "ExtensionFunctionType" }

        val typeArgs = type.arguments
        val returnType = renderType(typeArgs.last().type!!.resolve())
        val paramArgs = typeArgs.dropLast(1)

        return buildString {
            if (isComposable) append("@Composable ")
            if (isSuspend) append("suspend ")
            if (isExtension && paramArgs.isNotEmpty()) {
                append(renderType(paramArgs.first().type!!.resolve()))
                append(".")
                val rest = paramArgs.drop(1)
                append("(")
                append(rest.joinToString(", ") { renderType(it.type!!.resolve()) })
                append(")")
            } else {
                append("(")
                append(paramArgs.joinToString(", ") { renderType(it.type!!.resolve()) })
                append(")")
            }
            append(" -> ")
            append(returnType)
        }
    }
}
