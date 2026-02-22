---
name: theme-guide
description: >-
  This skill should be used for ANY Compose UI work: building screens,
  editing layouts, adding composables, using buttons/text/cards/dialogs,
  styling with colors/typography/shapes, creating UI components, or
  modifying existing screens. Relevant when the user says "add a button,"
  "create a screen," "change the color," "add a dialog," "style this
  component," or "use Material 3." All Compose UI must use :theme module
  wrappers instead of Material 3 directly.
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
3. **Consult the generated catalog** at `theme/build/generated/apidoc/api.md` to discover available components — prefer this over reading theme source files directly
4. **Create missing components** in `:theme` module if no wrapper exists (see creation guide in references)

## Component Catalog

Read `theme/build/generated/apidoc/api.md` for the complete, auto-generated component API with signatures, types, and file paths.

If the file does not exist, regenerate it:
```bash
./gradlew :theme:kspDebugKotlin
```

## Theming Quick Reference

Access theme tokens through `IndianaTheme`:

```kotlin
IndianaTheme.colorScheme.primary          // Color
IndianaTheme.colorScheme.onPrimary        // Color
IndianaTheme.colorScheme.surface          // Color
IndianaTheme.colorScheme.error            // Color
IndianaTheme.typography.headlineMedium    // TextStyle
IndianaTheme.typography.bodyLarge         // TextStyle
IndianaTheme.shapes.medium               // CornerBasedShape
```

### Error Color Scope

Wrap content in `IndianaTheme.Error { }` to remap `primary` colors to `error` colors. Components inside use error styling automatically:

```kotlin
IndianaTheme.Error {
    Button(text = "Delete", onClick = onDelete)  // renders in error colors
}
```

## Screen Pattern

Standard screen structure using scoped Scaffold APIs:

```kotlin
@Composable
fun MyScreen(
    navigateUp: () -> Unit,
    viewModel: MyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        // topBar receives a TopBarScope receiver — LargeFlexible and Sticky
        // are extension functions on TopBarScope, not standalone composables
        topBar = {
            LargeFlexible(
                title = { Text("Screen Title") },
                // navigationIcon receives a NavigationIconScope receiver —
                // Up() is defined on NavigationIconScope
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

### Screen with Pull-to-Refresh

```kotlin
@Composable
fun RefreshableScreen(viewModel: MyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
    )

    Scaffold(
        topBar = { LargeFlexible(title = { Text("Title") }) },
        pullToRefreshState = pullToRefreshState,
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            // content
        }
    }
}
```

## Key Scaffold APIs

All top-bar composables are **scoped** — call them inside the `topBar` lambda, not standalone.

| API | Scope | Purpose |
|-----|-------|---------|
| `LargeFlexible(title, subtitle?, navigationIcon?, actions?)` | `TopBarScope` | Collapsing top app bar with flexible height |
| `Sticky(content)` | `TopBarScope` | Content that follows the top bar collapse color |
| `Up(navigateUp)` | `NavigationIconScope` | Back arrow navigation icon |
| `rememberPullToRefreshState(isRefreshing, onRefresh)` | top-level | Create pull-to-refresh state to pass to `Scaffold` |

`Scaffold` automatically wires nested scroll behavior — no manual `nestedScroll` modifier needed.

## Creating New Components

Read `.claude/skills/theme-guide/references/creation-guide.md` before creating any new component in the `:theme` module.

## Reference Files

- Theme object & color/typography/shapes wrappers: `theme/src/main/java/com/davidmedenjak/indiana/theme/Theme.kt`
- Scaffold, TopBarScope, NavigationIconScope, pull-to-refresh: `theme/src/main/java/com/davidmedenjak/indiana/theme/ui/atoms/Scaffold.kt`
- LargeFlexibleTopAppBar (internal): `theme/src/main/java/com/davidmedenjak/indiana/theme/ui/atoms/TopBar.kt`
- Component creation guide: `.claude/skills/theme-guide/references/creation-guide.md`

## Verification

Regenerate the API catalog after adding or modifying components:
```bash
./gradlew :theme:kspDebugKotlin
```
