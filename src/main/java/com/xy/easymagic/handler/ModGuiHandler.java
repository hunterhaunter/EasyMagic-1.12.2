package com.xy.easymagic.handler;

import com.xy.easymagic.client.GuiEasyEnchantment;
import com.xy.easymagic.container.ContainerEasyEnchantment;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

public class ModGuiHandler implements IGuiHandler {

    public static final int ENCHANTMENT_TABLE_ID = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == ENCHANTMENT_TABLE_ID) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityEasyEnchantmentTable) {
                return new ContainerEasyEnchantment(
                    player,
                    (TileEntityEasyEnchantmentTable) te,
                    world,
                    new BlockPos(x, y, z)
                );
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == ENCHANTMENT_TABLE_ID) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityEasyEnchantmentTable) {
                return new GuiEasyEnchantment(
                    player,
                    (TileEntityEasyEnchantmentTable) te,
                    world,
                    new BlockPos(x, y, z)
                );
            }
        }
        return null;
    }
}
