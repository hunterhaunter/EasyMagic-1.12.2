# Changelog

## [1.2.2] - 2026-06-12

### Fixed
- Crash when opening an enchanting table (`NoClassDefFoundError: Could not initialize class GuiEnchantment`). The Enchantment Control soft-compat probe ran inside a mixin `static {}` block; Mixin's merge into the target class's `<clinit>` dropped the surrounding try/catch, so a missing `enchantmentcontrol` class escaped as `ExceptionInInitializerError` and permanently broke the enchant/reagent table GUIs.

### Changed
- Moved the Enchantment Control probe into a plain `EnchantmentControlCompat` class that fails safely (correct exception table, `Class.forName(..., false, loader)`, catches `Throwable`). The four duplicated probes are now one class. No config or gameplay changes; integration still works when Enchantment Control is present and no-ops cleanly when it is not.
