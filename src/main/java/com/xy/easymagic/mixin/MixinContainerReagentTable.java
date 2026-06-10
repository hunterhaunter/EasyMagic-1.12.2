package com.xy.easymagic.mixin;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageEnchantHints;
import com.xy.easymagic.network.PacketHandler;

import logictechcorp.reagenchant.inventory.ContainerReagentTable;
import logictechcorp.reagenchant.inventory.ReagentTableManager;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(ContainerReagentTable.class)
public abstract class MixinContainerReagentTable implements IEasyMagicContainer {

    @Shadow(remap = false)
    private ReagentTableManager reagentTableManager;

    @Override
    public BlockPos easymagic$getPosition() {
        return this.reagentTableManager.getPos();
    }

    @Override
    public boolean easymagic$canReroll(boolean isCreative) {
        ItemStack tableItem = this.reagentTableManager.getInventory().getStackInSlot(0);
        if (tableItem.isEmpty() || tableItem.isItemEnchanted()) {
            return false;
        }
        if (!isCreative) {
            int lapisCost = EasyMagicConfig.rerollLapisCost;
            if (lapisCost > 0) {
                if (this.reagentTableManager.getLapisAmount() < lapisCost) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack easymagic$getTableItem() {
        return this.reagentTableManager.getInventory().getStackInSlot(0);
    }

    @Override
    public ItemStack easymagic$getLapisItem() {
        return this.reagentTableManager.getInventory().getStackInSlot(1);
    }

    @Override
    public void easymagic$performReroll(EntityPlayerMP player, int newSeed, boolean isCreative) {
        if (!isCreative) {
            int lapisCost = EasyMagicConfig.rerollLapisCost;
            if (lapisCost > 0) {
                this.reagentTableManager.getInventory().extractItem(1, lapisCost, false);
            }
        }

        // Reflectively set xpSeed on ReagentTableManager (package-private field)
        try {
            Field xpSeedField = ReagentTableManager.class.getDeclaredField("xpSeed");
            xpSeedField.setAccessible(true);
            xpSeedField.setInt(this.reagentTableManager, newSeed);
        } catch (Exception e) {
            EasyMagic.LOGGER.error("Failed to set xpSeed on ReagentTableManager: {}", e.getMessage());
        }

        // Reflectively call onContentsChanged (package-private method)
        try {
            Method onContentsChanged = ReagentTableManager.class.getDeclaredMethod(
                "onContentsChanged", ContainerReagentTable.class);
            onContentsChanged.setAccessible(true);
            onContentsChanged.invoke(this.reagentTableManager, (ContainerReagentTable)(Object)this);
        } catch (Exception e) {
            EasyMagic.LOGGER.error("Failed to invoke onContentsChanged on ReagentTableManager: {}", e.getMessage());
        }
    }

    @Unique
    private static final Method easymagic$enchTableThreadLocalSet;

    static {
        Method m = null;
        try {
            Class<?> cls = Class.forName("enchantmentcontrol.util.FromEnchTableThreadLocal");
            m = cls.getMethod("set", boolean.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        easymagic$enchTableThreadLocalSet = m;
    }

    @Unique
    private int easymagic$lastXpSeed = Integer.MIN_VALUE;
    @Unique
    private int[] easymagic$lastLevels = new int[3];

    @Inject(method = "detectAndSendChanges", at = @At("RETURN"))
    private void easymagic$checkAndSendHints(CallbackInfo ci) {
        int currentSeed = this.reagentTableManager.getXpSeed();
        int[] currentLevels = this.reagentTableManager.getEnchantabilityLevels();
        if (currentSeed == easymagic$lastXpSeed && Arrays.equals(currentLevels, easymagic$lastLevels)) return;
        easymagic$lastXpSeed = currentSeed;
        System.arraycopy(currentLevels, 0, easymagic$lastLevels, 0, 3);

        EntityPlayerMP player = easymagic$findPlayer();
        if (player == null) return;

        ItemStack stack = this.reagentTableManager.getInventory().getStackInSlot(0);
        if (stack.isEmpty()) {
            PacketHandler.INSTANCE.sendTo(
                new MessageEnchantHints(((Container)(Object)this).windowId,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                player);
            return;
        }
        List<EnchantmentData> slot0 = easymagic$buildSlotHints(stack, 0, currentLevels, currentSeed);
        List<EnchantmentData> slot1 = easymagic$buildSlotHints(stack, 1, currentLevels, currentSeed);
        List<EnchantmentData> slot2 = easymagic$buildSlotHints(stack, 2, currentLevels, currentSeed);
        PacketHandler.INSTANCE.sendTo(
            new MessageEnchantHints(((Container)(Object)this).windowId, slot0, slot1, slot2), player);
    }

    @Unique
    private EntityPlayerMP easymagic$findPlayer() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return null;
        Container self = (Container)(Object)this;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player.openContainer == self) return player;
        }
        return null;
    }

    @Unique
    private List<EnchantmentData> easymagic$buildSlotHints(ItemStack stack, int slot, int[] levels, int seed) {
        int level = levels[slot];
        if (level <= 0) return Collections.emptyList();
        easymagic$markEnchTableContext();
        Random rand = new Random((long) (seed + slot));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(rand, stack, level, false);
        if (list == null) return Collections.emptyList();
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(rand.nextInt(list.size()));
        }
        return list;
    }

    @Unique
    private static void easymagic$markEnchTableContext() {
        if (easymagic$enchTableThreadLocalSet != null) {
            try {
                easymagic$enchTableThreadLocalSet.invoke(null, true);
            } catch (Exception ignored) {
            }
        }
    }
}
