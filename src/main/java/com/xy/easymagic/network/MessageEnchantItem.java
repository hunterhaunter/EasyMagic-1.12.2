package com.xy.easymagic.network;

import com.xy.easymagic.container.ContainerEasyEnchantment;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEnchantItem implements IMessage {

    private BlockPos pos;
    private int slotIndex;

    public MessageEnchantItem() {
        this.pos = BlockPos.ORIGIN;
        this.slotIndex = 0;
    }

    public MessageEnchantItem(BlockPos pos, int slotIndex) {
        this.pos = pos;
        this.slotIndex = slotIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(slotIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.slotIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<MessageEnchantItem, IMessage> {

        @Override
        public IMessage onMessage(MessageEnchantItem message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.getDistanceSq(message.pos) > 64.0) {
                    return;
                }

                if (player.openContainer instanceof ContainerEasyEnchantment) {
                    ContainerEasyEnchantment container =
                        (ContainerEasyEnchantment) player.openContainer;
                    container.enchantItem(player, message.slotIndex);
                }
            });
            return null;
        }
    }
}
