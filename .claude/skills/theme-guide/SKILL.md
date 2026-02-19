---
name: theme-guide
description: Theme component reference for the :theme module. Use when building screens or modifying UI.
disable-model-invocation: false
allowed-tools:
  - Bash(./gradlew :theme:kspDebugKotlin*)
  - Edit
  - Write
  - Read
  - Glob
  - Grep
---

# Theme Guide

## Rules

1. **Never use Material 3 directly** — always use `:theme` module wrappers
2. **Use `IndianaTheme.*`** not `MaterialTheme.*` for colors, typography, shapes
3. **Don't read theme source files** to discover components — use the generated catalog below
4. **Create missing components** in `:theme` module if no wrapper exists (see creation guide)

## Component Catalog

Read `theme/build/generated/apidoc/api.md` for the complete, auto-generated component API with signatures, types, and file paths.

If the file doesn't exist, run:
```bash
./gradlew :theme:kspDebugKotlin
```

## Screen Pattern

Standard screen structure:

```kotlin
@Composable
fun MyScreen(
    navigateUp: () -> Unit,
    viewModel: MyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text("Screen Title") },
                navigationIcon = { Up(navigateUp) },
            )
        },
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            // content
        }
    }
}
```

## Creating New Components

Read `.claude/skills/theme-guide/creation-guide.md` before creating any new component in the `:theme` module.
