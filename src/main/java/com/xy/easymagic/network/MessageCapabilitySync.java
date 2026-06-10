package com.xy.easymagic.network;

import com.xy.easymagic.capability.EasyMagicItemHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MessageCapabilitySync implements IMessage {

    private BlockPos pos;
    private ItemStack item;
    private ItemStack lapis;

    public MessageCapabilitySync() {
        this.pos = BlockPos.ORIGIN;
        this.item = ItemStack.EMPTY;
        this.lapis = ItemStack.EMPTY;
    }

    public MessageCapabilitySync(BlockPos pos, ItemStack item, ItemStack lapis) {
        this.pos = pos;
        this.item = item;
        this.lapis = lapis;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeItemStack(buf, item);
        ByteBufUtils.writeItemStack(buf, lapis);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.item = ByteBufUtils.readItemStack(buf);
        this.lapis = ByteBufUtils.readItemStack(buf);
    }

    public static class Handler implements IMessageHandler<MessageCapabilitySync, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageCapabilitySync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                net.minecraft.world.World world = Minecraft.getMinecraft().world;
                if (world == null) return;
                TileEntity te = world.getTileEntity(message.pos);
                if (te == null) return;
                IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (!(cap instanceof EasyMagicItemHandler)) return;
                EasyMagicItemHandler handler = (EasyMagicItemHandler) cap;
                handler.setStackInSlot(0, message.item);
                handler.setStackInSlot(1, message.lapis);
            });
            return null;
        }
    }
}
