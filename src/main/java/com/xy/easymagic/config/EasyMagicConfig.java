package com.xy.easymagic.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class EasyMagicConfig {

    public static boolean keepItemsInTable = true;
    public static boolean showEnchantmentHints = true;
    public static boolean rerollEnabled = true;
    public static int rerollXpCost = 5;
    public static int rerollLapisCost = 1;
    public static int maxBookshelfCount = 15;
    public static int enchantmentHintCount = -1;

    private static Configuration config;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        config.load();

        keepItemsInTable = config.getBoolean(
            "keepItemsInTable",
            Configuration.CATEGORY_GENERAL,
            true,
            "Items stay in the enchanting table when the GUI is closed"
        );
        showEnchantmentHints = config.getBoolean(
            "showEnchantmentHints",
            Configuration.CATEGORY_GENERAL,
            true,
            "Show enchantment name hints in the enchanting GUI"
        );
        rerollEnabled = config.getBoolean(
            "rerollEnabled",
            Configuration.CATEGORY_GENERAL,
            true,
            "Enable the reroll button in the enchanting GUI"
        );
        rerollXpCost = config.getInt(
            "rerollXpCost",
            Configuration.CATEGORY_GENERAL,
            5,
            0, 1000,
            "XP points (not levels) required to reroll enchantments (0 = free)"
        );
        rerollLapisCost = config.getInt(
            "rerollLapisCost",
            Configuration.CATEGORY_GENERAL,
            1,
            0, 64,
            "Lapis lazuli required to reroll enchantments (0 = free)"
        );
        maxBookshelfCount = config.getInt(
            "maxBookshelfCount",
            Configuration.CATEGORY_GENERAL,
            15,
            0, 15,
            "Number of bookshelves required for maximum level enchantments"
        );
        enchantmentHintCount = config.getInt(
            "enchantmentHintCount",
            Configuration.CATEGORY_GENERAL,
            -1,
            -1, 20,
            "How many enchantments to preview in the tooltip (-1 = show all, 1 = vanilla behavior)"
        );

        if (config.hasChanged()) {
            config.save();
        }
    }
}
