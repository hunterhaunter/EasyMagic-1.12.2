package com.xy.easymagic.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class MessageEnchantHints implements IMessage {

    private int windowId;
    private List<EnchantmentData>[] slotEnchants;

    public MessageEnchantHints() {
    }

    @SuppressWarnings("unchecked")
    public MessageEnchantHints(int windowId, List<EnchantmentData> slot0, List<EnchantmentData> slot1, List<EnchantmentData> slot2) {
        this.windowId = windowId;
        this.slotEnchants = new List[]{slot0, slot1, slot2};
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        for (int slot = 0; slot < 3; slot++) {
            List<EnchantmentData> list = slotEnchants[slot];
            buf.writeByte(list.size());
            for (EnchantmentData data : list) {
                buf.writeShort(Enchantment.getEnchantmentID(data.enchantment));
                buf.writeByte(data.enchantmentLevel);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromBytes(ByteBuf buf) {
        this.windowId = buf.readInt();
        this.slotEnchants = new List[3];
        for (int slot = 0; slot < 3; slot++) {
            int size = buf.readByte() & 0xFF;
            List<EnchantmentData> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int id = buf.readShort();
                int level = buf.readByte();
                Enchantment ench = Enchantment.getEnchantmentByID(id);
                if (ench != null) {
                    list.add(new EnchantmentData(ench, level));
                }
            }
            slotEnchants[slot] = list;
        }
    }

    @SideOnly(Side.CLIENT)
    private static List<EnchantmentData>[] clientHints;
    @SideOnly(Side.CLIENT)
    private static int clientHintsWindowId = -1;

    @SideOnly(Side.CLIENT)
    public static List<EnchantmentData> getHints(int windowId, int slot) {
        if (clientHintsWindowId == windowId && clientHints != null && slot >= 0 && slot < 3) {
            return clientHints[slot];
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void clearHints() {
        clientHints = null;
        clientHintsWindowId = -1;
    }

    public static class Handler implements IMessageHandler<MessageEnchantHints, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageEnchantHints message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                clientHints = message.slotEnchants;
                clientHintsWindowId = message.windowId;
            });
            return null;
        }
    }
}
