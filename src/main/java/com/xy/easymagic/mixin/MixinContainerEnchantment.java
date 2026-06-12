package com.xy.easymagic.mixin;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.LapisUtil;
import com.xy.easymagic.capability.EasyMagicItemHandler;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageCapabilitySync;
import com.xy.easymagic.network.PacketHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerEnchantment.class)
public abstract class MixinContainerEnchantment implements IEasyMagicContainer {

    @Shadow
    public IInventory tableInventory;

    @Shadow
    private World world;

    @Shadow
    private BlockPos position;

    @Shadow
    public int xpSeed;

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
            if (lapisCost > 0
                    && LapisUtil.getLapisCount(this.tableInventory.getStackInSlot(1)) < lapisCost) {
                return false;
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
            if (lapisCost > 0 && LapisUtil.isLapis(this.tableInventory.getStackInSlot(1))) {
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
        if (worldIn.isRemote) return;
        // Modpacks can swap the enchanting table tile entity; never let a
        // failure here abort the container constructor or the GUI won't open.
        try {
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
        } catch (Exception e) {
            EasyMagic.LOGGER.error("Failed to restore enchanting table items at {}", pos, e);
        }
    }

    @Inject(method = "onContainerClosed", at = @At("HEAD"))
    private void easymagic$saveItemsOnClose(EntityPlayer playerIn, CallbackInfo ci) {
        if (this.world.isRemote) return;
        if (!EasyMagicConfig.keepItemsInTable) return;
        try {
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
        } catch (Exception e) {
            EasyMagic.LOGGER.error("Failed to store enchanting table items at {}", this.position, e);
        }
    }

    @Unique
    private static void easymagic$syncCapability(World world, BlockPos pos, EasyMagicItemHandler handler) {
        PacketHandler.INSTANCE.sendToAllAround(
            new MessageCapabilitySync(pos, handler.getStackInSlot(0), handler.getStackInSlot(1)),
            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64)
        );
    }
}
