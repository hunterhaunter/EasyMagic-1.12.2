package com.xy.easymagic.block;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.handler.ModGuiHandler;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockEasyEnchantmentTable extends BlockEnchantmentTable {

    public BlockEasyEnchantmentTable() {
        super();
        this.setLightOpacity(0);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEasyEnchantmentTable();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityEasyEnchantmentTable();
    }

    @Override
    public boolean onBlockActivated(
        World worldIn,
        BlockPos pos,
        IBlockState state,
        EntityPlayer playerIn,
        EnumHand hand,
        EnumFacing facing,
        float hitX,
        float hitY,
        float hitZ
    ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEasyEnchantmentTable) {
                playerIn.openGui(
                    EasyMagic.instance,
                    ModGuiHandler.ENCHANTMENT_TABLE_ID,
                    worldIn,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
                );
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEasyEnchantmentTable) {
            TileEntityEasyEnchantmentTable enchTable =
                (TileEntityEasyEnchantmentTable) te;
            InventoryHelper.dropInventoryItems(worldIn, pos, enchTable);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }
}
