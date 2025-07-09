# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this module.

## Module overview

This module provides custom design system components for the app.
- Followes Atomic design pattern (atoms, molecules, organisms)
- IndianaTheme wraps Material 3 theming with custom colors and typography

## Structure

- Include a Preview for every design component showcasing different states
- Keep all previews private
- Use import aliases with `M3` prefix for material3 components, e.g. `import androidx.compose.material3.Button as M3Button`
