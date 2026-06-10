package com.xy.easymagic.mixin;

import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.capability.EasyMagicItemHandler;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageCapabilitySync;
import com.xy.easymagic.network.MessageEnchantHints;
import com.xy.easymagic.network.PacketHandler;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(ContainerEnchantment.class)
public abstract class MixinContainerEnchantment implements IEasyMagicContainer {

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
    private BlockPos position;

    @Shadow
    public int xpSeed;

    @Shadow
    public int[] enchantLevels;

    @Unique
    private EntityPlayerMP easymagic$player;

    @Override
    public BlockPos easymagic$getPosition() {
        return this.position;
    }

    @Override
    public boolean easymagic$canReroll(boolean isCreative) {
        ItemStack tableItem = this.tableInventory.getStackInSlot(0);
        if (tableItem.isEmpty() || tableItem.isItemEnchanted()) {
            return false;
        }
        if (!isCreative) {
            int lapisCost = EasyMagicConfig.rerollLapisCost;
            if (lapisCost > 0) {
                ItemStack lapisStack = this.tableInventory.getStackInSlot(1);
                if (lapisStack.isEmpty() || lapisStack.getCount() < lapisCost) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack easymagic$getTableItem() {
        return this.tableInventory.getStackInSlot(0);
    }

    @Override
    public ItemStack easymagic$getLapisItem() {
        return this.tableInventory.getStackInSlot(1);
    }

    @Override
    public void easymagic$performReroll(EntityPlayerMP player, int newSeed, boolean isCreative) {
        if (!isCreative) {
            int lapisCost = EasyMagicConfig.rerollLapisCost;
            if (lapisCost > 0) {
                this.tableInventory.decrStackSize(1, lapisCost);
            }
        }
        this.xpSeed = newSeed;
        ((ContainerEnchantment)(Object)this).onCraftMatrixChanged(this.tableInventory);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
        at = @At("TAIL")
    )
    private void easymagic$loadCapabilityItems(InventoryPlayer playerInv, World worldIn, BlockPos pos, CallbackInfo ci) {
        if (playerInv.player instanceof EntityPlayerMP) {
            this.easymagic$player = (EntityPlayerMP) playerInv.player;
        }
        if (worldIn.isRemote) return;
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null) return;
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(cap instanceof EasyMagicItemHandler)) return;
        EasyMagicItemHandler handler = (EasyMagicItemHandler) cap;
        for (int i = 0; i < 2; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                this.tableInventory.setInventorySlotContents(i, stack.copy());
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        te.markDirty();
        easymagic$syncCapability(worldIn, pos, handler);
    }

    @Inject(method = "onContainerClosed", at = @At("HEAD"))
    private void easymagic$saveItemsOnClose(EntityPlayer playerIn, CallbackInfo ci) {
        if (this.world.isRemote) return;
        if (!EasyMagicConfig.keepItemsInTable) return;
        TileEntity te = this.world.getTileEntity(this.position);
        if (te == null) return;
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(cap instanceof EasyMagicItemHandler)) return;
        EasyMagicItemHandler handler = (EasyMagicItemHandler) cap;
        for (int i = 0; i < 2; i++) {
            ItemStack stack = this.tableInventory.getStackInSlot(i);
            handler.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            this.tableInventory.removeStackFromSlot(i);
        }
        te.markDirty();
        easymagic$syncCapability(this.world, this.position, handler);
    }

    @ModifyArg(
        method = "broadcastData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/IContainerListener;sendWindowProperty(Lnet/minecraft/inventory/Container;II)V",
            ordinal = 3
        ),
        index = 2
    )
    private int easymagic$sendFullXpSeed(int maskedSeed) {
        return this.xpSeed;
    }

    @Unique
    private static void easymagic$syncCapability(World world, BlockPos pos, EasyMagicItemHandler handler) {
        PacketHandler.INSTANCE.sendToAllAround(
            new MessageCapabilitySync(pos, handler.getStackInSlot(0), handler.getStackInSlot(1)),
            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64)
        );
    }

    @Inject(method = "onCraftMatrixChanged", at = @At("RETURN"))
    private void easymagic$sendEnchantHints(IInventory inventoryIn, CallbackInfo ci) {
        if (this.world.isRemote || this.easymagic$player == null) return;
        ItemStack stack = this.tableInventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            PacketHandler.INSTANCE.sendTo(
                new MessageEnchantHints(((Container)(Object)this).windowId,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                this.easymagic$player);
            return;
        }
        List<EnchantmentData> slot0 = easymagic$buildSlotHints(stack, 0);
        List<EnchantmentData> slot1 = easymagic$buildSlotHints(stack, 1);
        List<EnchantmentData> slot2 = easymagic$buildSlotHints(stack, 2);
        PacketHandler.INSTANCE.sendTo(
            new MessageEnchantHints(((Container)(Object)this).windowId, slot0, slot1, slot2),
            this.easymagic$player);
    }

    @Unique
    @SuppressWarnings("unchecked")
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
