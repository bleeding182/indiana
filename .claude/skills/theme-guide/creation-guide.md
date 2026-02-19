# Theme Component Creation Guide

## M3 Alias Wrapping Pattern

Every theme component wraps a Material 3 component using an import alias with `M3` prefix.

### Template

```kotlin
package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import com.davidmedenjak.apidoc.Usage
import androidx.compose.material3.SomeComponent as M3SomeComponent

@Usage("Brief description of when/how to use this component")
@Composable
fun SomeComponent(
    // Required params first
    text: String,
    onClick: () -> Unit,
    // Modifier always has default
    modifier: Modifier = Modifier,
    // Optional params with defaults
    enabled: Boolean = true,
) = M3SomeComponent(
    modifier = modifier,
    enabled = enabled,
    onClick = onClick,
) { Text(text) }

@Composable
@PreviewLightDark
private fun Preview() {
    PreviewSurface {
        SomeComponent(text = "Example", onClick = {})
    }
}
```

## File Placement

| Type | Directory |
|------|-----------|
| Single-purpose UI elements (Button, Text, Switch) | `ui/atoms/` |
| Composed elements (Dialog, PropertyLayout) | `ui/molectule/` |
| Modifier extensions | `ui/modifier/` |
| Preview utilities | `ui/preview/` |

## Preview Conventions

- Annotate with `@PreviewLightDark` (not `@Preview`)
- Make all preview functions `private`
- Wrap content in `PreviewSurface { }` for simple components
- Wrap content in `PreviewScreen { }` for full-screen previews (e.g. Scaffold)
- Use `PressedInteractionSource` to show pressed states
- Show multiple states in a single preview using `Row`/`Column`

## `@Usage` Annotation

Add `@Usage("description")` to public composables to document intent in the auto-generated API catalog. The description appears as a blockquote under the function signature in `api.md`.

## Checklist

1. Create wrapper function in appropriate `ui/` subdirectory
2. Import M3 component with `as M3` prefix alias
3. Add `@Usage` annotation with brief description
4. Add `@PreviewLightDark` private preview
5. Use the new component from `:app` module
