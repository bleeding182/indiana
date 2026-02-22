# CLAUDE.md

## Module overview

This module provides custom design system components for the app.
- Follows Atomic design pattern (atoms, molecules)
- IndianaTheme wraps Material 3 theming with custom colors and typography

## Structure

- Include a Preview for every design component showcasing different states
- Keep all previews private
- Use import aliases with `M3` prefix for material3 components, e.g. `import androidx.compose.material3.Button as M3Button`

## Commands

```bash
# Regenerate the auto-generated component API catalog
./gradlew :theme:kspDebugKotlin

# Run lint checks (includes custom checks from :lint module)
./gradlew :theme:lintDebug
```
