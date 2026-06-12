package com.xy.easymagic.mixin;

import com.xy.easymagic.network.MessageEnchantHints;
import com.xy.easymagic.network.PacketHandler;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Server-side enchantment hint syncing. Kept separate from
 * MixinContainerEnchantment because it depends on the vanilla behavior of
 * onCraftMatrixChanged, which Apotheosis rewrites with its own coremod.
 * EasyMagicMixinPlugin skips this mixin entirely when Apotheosis is present.
 */
@Mixin(ContainerEnchantment.class)
public abstract class MixinContainerEnchantmentHints {

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

    @Shadow
    public IInventory tableInventory;

    @Shadow
    private World world;

    @Shadow
    public int xpSeed;

    @Shadow
    public int[] enchantLevels;

    @Unique
    private EntityPlayerMP easymagic$hintPlayer;

    @Inject(
        method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
        at = @At("TAIL")
    )
    private void easymagic$capturePlayer(InventoryPlayer playerInv, World worldIn, BlockPos pos, CallbackInfo ci) {
        if (playerInv.player instanceof EntityPlayerMP) {
            this.easymagic$hintPlayer = (EntityPlayerMP) playerInv.player;
        }
    }

    // The vanilla packet masks the seed; hints need the real value so the
    // client fallback computation matches the server. require = 0 so another
    // mod transforming broadcastData degrades hints instead of killing the GUI.
    @ModifyArg(
        method = "broadcastData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/IContainerListener;sendWindowProperty(Lnet/minecraft/inventory/Container;II)V",
            ordinal = 3
        ),
        index = 2,
        require = 0
    )
    private int easymagic$sendFullXpSeed(int maskedSeed) {
        return this.xpSeed;
    }

    @Inject(method = "onCraftMatrixChanged", at = @At("RETURN"))
    private void easymagic$sendEnchantHints(IInventory inventoryIn, CallbackInfo ci) {
        if (this.world.isRemote || this.easymagic$hintPlayer == null) return;
        ItemStack stack = this.tableInventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            PacketHandler.INSTANCE.sendTo(
                new MessageEnchantHints(((Container)(Object)this).windowId,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                this.easymagic$hintPlayer);
            return;
        }
        List<EnchantmentData> slot0 = easymagic$buildSlotHints(stack, 0);
        List<EnchantmentData> slot1 = easymagic$buildSlotHints(stack, 1);
        List<EnchantmentData> slot2 = easymagic$buildSlotHints(stack, 2);
        PacketHandler.INSTANCE.sendTo(
            new MessageEnchantHints(((Container)(Object)this).windowId, slot0, slot1, slot2),
            this.easymagic$hintPlayer);
    }

    @Unique
    private List<EnchantmentData> easymagic$buildSlotHints(ItemStack stack, int slot) {
        int level = this.enchantLevels[slot];
        if (level <= 0) return Collections.emptyList();
        easymagic$markEnchTableContext();
        Random rand = new Random((long) (this.xpSeed + slot));
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
