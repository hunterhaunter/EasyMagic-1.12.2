package com.xy.easymagic.network;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.config.EasyMagicConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class MessageReroll implements IMessage {

    public MessageReroll() {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageReroll, IMessage> {

        private static final String[] ENCHANTMENT_SEED_NAMES = {
            "xpSeed",
            "field_175152_f"
        };

        @Override
        public IMessage onMessage(MessageReroll message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof IEasyMagicContainer)) {
                    return;
                }

                IEasyMagicContainer emc = (IEasyMagicContainer) player.openContainer;
                BlockPos pos = emc.easymagic$getPosition();

                if (player.getDistanceSq(pos) > 64.0) {
                    return;
                }

                boolean creative = player.capabilities.isCreativeMode;

                if (!emc.easymagic$canReroll(creative)) {
                    return;
                }

                // XP check (common to both container types)
                if (!creative) {
                    int xpCost = EasyMagicConfig.rerollXpCost;
                    if (xpCost > 0 && player.experienceTotal < xpCost) {
                        return;
                    }
                    if (xpCost > 0) {
                        removeXpPoints(player, xpCost);
                    }
                }

                int newSeed = player.world.rand.nextInt();

                // Set player's xpSeed field (same reflection as before)
                for (String fieldName : ENCHANTMENT_SEED_NAMES) {
                    try {
                        Field seedField = ReflectionHelper.findField(
                            EntityPlayer.class,
                            fieldName
                        );
                        seedField.setAccessible(true);
                        seedField.setInt(player, newSeed);
                        break;
                    } catch (Exception e) {
                        EasyMagic.LOGGER.warn("Failed to set xpSeed via '{}': {}", fieldName, e.getMessage());
                    }
                }

                // Delegate container-specific work
                emc.easymagic$performReroll(player, newSeed, creative);
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
