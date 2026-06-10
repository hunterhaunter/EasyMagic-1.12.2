package com.xy.easymagic.mixin;

import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.capability.EasyMagicItemHandler;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageCapabilitySync;
import com.xy.easymagic.network.PacketHandler;

import net.minecraft.entity.player.EntityPlayer;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @Inject(
        method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
        at = @At("TAIL")
    )
    private void easymagic$loadCapabilityItems(InventoryPlayer playerInv, World worldIn, BlockPos pos, CallbackInfo ci) {
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
}
