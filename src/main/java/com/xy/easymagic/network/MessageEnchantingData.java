package com.xy.easymagic.network;

import com.xy.easymagic.client.GuiEasyEnchantment;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageEnchantingData implements IMessage {

    private int[] enchantClue;
    private int[] worldClue;
    private int[] enchantLevels;

    public MessageEnchantingData() {
        this.enchantClue = new int[3];
        this.worldClue = new int[3];
        this.enchantLevels = new int[3];
    }

    public MessageEnchantingData(int[] enchantClue, int[] worldClue, int[] enchantLevels) {
        this.enchantClue = enchantClue;
        this.worldClue = worldClue;
        this.enchantLevels = enchantLevels;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (int i = 0; i < 3; i++) {
            buf.writeInt(enchantClue[i]);
        }
        for (int i = 0; i < 3; i++) {
            buf.writeInt(worldClue[i]);
        }
        for (int i = 0; i < 3; i++) {
            buf.writeInt(enchantLevels[i]);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.enchantClue = new int[3];
        this.worldClue = new int[3];
        this.enchantLevels = new int[3];
        for (int i = 0; i < 3; i++) {
            enchantClue[i] = buf.readInt();
        }
        for (int i = 0; i < 3; i++) {
            worldClue[i] = buf.readInt();
        }
        for (int i = 0; i < 3; i++) {
            enchantLevels[i] = buf.readInt();
        }
    }

    public int[] getEnchantClue() {
        return enchantClue;
    }

    public int[] getWorldClue() {
        return worldClue;
    }

    public int[] getEnchantLevels() {
        return enchantLevels;
    }

    public static class Handler implements IMessageHandler<MessageEnchantingData, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageEnchantingData message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (mc.currentScreen instanceof GuiEasyEnchantment) {
                    ((GuiEasyEnchantment) mc.currentScreen).updateEnchantmentData(message);
                }
            });
            return null;
        }
    }
}
