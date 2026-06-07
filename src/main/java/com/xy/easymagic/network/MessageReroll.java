package com.xy.easymagic.network;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.container.ContainerEasyEnchantment;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class MessageReroll implements IMessage {

    private BlockPos pos;

    public MessageReroll() {
        this.pos = BlockPos.ORIGIN;
    }

    public MessageReroll(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<MessageReroll, IMessage> {

        private static final String[] ENCHANTMENT_SEED_NAMES = {
            "xpSeed",               // MCP
            "field_175152_f"         // SRG
        };

        @Override
        public IMessage onMessage(MessageReroll message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.getDistanceSq(message.pos) > 64.0) {
                    return;
                }

                TileEntity te = player.world.getTileEntity(message.pos);
                if (!(te instanceof TileEntityEasyEnchantmentTable)) {
                    return;
                }

                TileEntityEasyEnchantmentTable table = (TileEntityEasyEnchantmentTable) te;

                ItemStack tableItem = table.getStackInSlot(0);
                if (tableItem.isEmpty() || tableItem.isItemEnchanted()) {
                    return;
                }

                int lapisCost = EasyMagicConfig.rerollLapisCost;
                if (lapisCost > 0) {
                    ItemStack lapisStack = table.getStackInSlot(1);
                    if (lapisStack.isEmpty() || lapisStack.getCount() < lapisCost) {
                        return;
                    }
                }

                if (!player.capabilities.isCreativeMode) {
                    int xpCost = EasyMagicConfig.rerollXpCost;
                    if (xpCost > 0 && player.experienceTotal < xpCost) {
                        return;
                    }

                    if (xpCost > 0) {
                        removeXpPoints(player, xpCost);
                    }

                    if (lapisCost > 0) {
                        table.decrStackSize(1, lapisCost);
                        table.markDirty();
                    }
                }

                int newSeed = player.world.rand.nextInt();
                boolean set = false;
                for (String fieldName : ENCHANTMENT_SEED_NAMES) {
                    try {
                        Field seedField = ReflectionHelper.findField(
                            net.minecraft.entity.player.EntityPlayer.class,
                            fieldName
                        );
                        seedField.setAccessible(true);
                        seedField.setInt(player, newSeed);
                        set = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }

                if (!set) {
                    EasyMagic.LOGGER.error(
                        "Failed to set enchantment seed via reflection"
                    );
                    return;
                }

                if (player.openContainer instanceof ContainerEasyEnchantment) {
                    ((ContainerEasyEnchantment) player.openContainer)
                        .recalculateEnchantments();
                }
            });
            return null;
        }

        private static void removeXpPoints(EntityPlayerMP player, int points) {
            player.experienceTotal = Math.max(0, player.experienceTotal - points);
            player.experienceLevel = 0;
            player.experience = 0.0F;
            int remaining = player.experienceTotal;
            while (remaining > 0) {
                int cap = player.xpBarCap();
                if (remaining >= cap) {
                    remaining -= cap;
                    player.experienceLevel++;
                } else {
                    player.experience = (float) remaining / (float) player.xpBarCap();
                    break;
                }
            }
        }
    }
}
