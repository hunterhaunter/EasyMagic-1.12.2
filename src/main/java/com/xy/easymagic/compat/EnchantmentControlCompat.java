package com.xy.easymagic.compat;

import java.lang.reflect.Method;

/**
 * Soft-dependency bridge to the Enchantment Control mod (package
 * {@code enchantmentcontrol}). When that mod is present, marking its
 * "from enchanting table" thread-local makes its enchantment filtering apply to
 * the enchantment lists EasyMagic rolls for hints and rerolls. When it is
 * absent, every method here is a no-op.
 *
 * <p>Why this lives in a plain class instead of a mixin {@code static {}} block:
 * Mixin merges a mixin's static initializer into the <em>target</em> class's
 * {@code <clinit>}, and the try/catch exception-handler range does not survive
 * that merge reliably. The probe's {@code ClassNotFoundException} then escaped
 * the (source-level) catch and surfaced as {@code ExceptionInInitializerError}
 * from the merged {@code <clinit>}, permanently bricking the target — e.g.
 * {@code GuiEnchantment} could never initialize, so opening an enchanting table
 * crashed the game. A normal class is compiled by javac with a correct
 * exception table, so the probe fails safely here.
 *
 * <p>The lookup uses {@code Class.forName(name, false, loader)} so the probe
 * never <em>initializes</em> the foreign class; it is only initialized later if
 * the mod is actually present and {@link #markEnchTableContext()} invokes it.
 */
public final class EnchantmentControlCompat {

    private static final Method ENCH_TABLE_THREAD_LOCAL_SET = resolveSetter();

    private EnchantmentControlCompat() {
    }

    private static Method resolveSetter() {
        try {
            Class<?> cls = Class.forName(
                    "enchantmentcontrol.util.FromEnchTableThreadLocal",
                    false,
                    EnchantmentControlCompat.class.getClassLoader());
            return cls.getMethod("set", boolean.class);
        } catch (Throwable ignored) {
            // Mod absent (or its class failed to load/transform). Treat as not present.
            return null;
        }
    }

    /**
     * Flags the current enchantment roll as originating from an enchanting table
     * so Enchantment Control applies its table-only filtering. No-op when the
     * mod is absent.
     */
    public static void markEnchTableContext() {
        if (ENCH_TABLE_THREAD_LOCAL_SET == null) {
            return;
        }
        try {
            ENCH_TABLE_THREAD_LOCAL_SET.invoke(null, true);
        } catch (Throwable ignored) {
        }
    }
}
