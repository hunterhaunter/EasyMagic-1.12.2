package com.xy.easymagic.container;

import com.xy.easymagic.EasyMagic;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageEnchantingData;
import com.xy.easymagic.network.PacketHandler;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;
import java.util.Random;

public class ContainerEasyEnchantment extends Container {

    private final TileEntityEasyEnchantmentTable tableInventory;
    private final World world;
    private final BlockPos pos;
    private final EntityPlayer player;
    private final Random rand;

    public int[] enchantLevels = new int[3];
    public int[] enchantClue = new int[]{-1, -1, -1};
    public int[] worldClue = new int[]{-1, -1, -1};

    private int[] lastEnchantLevels = new int[]{-1, -1, -1};
    private int[] lastEnchantClue = new int[]{-2, -2, -2};
    private int[] lastWorldClue = new int[]{-2, -2, -2};

    private static final String[] ENCHANTMENT_SEED_NAMES = {
        "xpSeed",
        "field_175152_f"
    };

    public ContainerEasyEnchantment(
        EntityPlayer player,
        TileEntityEasyEnchantmentTable tableInventory,
        World world,
        BlockPos pos
    ) {
        this.player = player;
        this.tableInventory = tableInventory;
        this.world = world;
        this.pos = pos;
        this.rand = new Random();

        this.addSlotToContainer(new Slot(tableInventory, 0, 15, 47) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                ContainerEasyEnchantment.this.onCraftMatrixChanged(tableInventory);
            }
        });

        this.addSlotToContainer(new Slot(tableInventory, 1, 35, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.DYE
                    && stack.getMetadata() == 4;
            }

            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                ContainerEasyEnchantment.this.onCraftMatrixChanged(tableInventory);
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(
                    player.inventory,
                    col + row * 9 + 9,
                    8 + col * 18,
                    84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(
                player.inventory,
                col,
                8 + col * 18,
                142
            ));
        }

        computeEnchantmentData();
    }

    @Override
    public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventoryIn) {
        computeEnchantmentData();
        super.onCraftMatrixChanged(inventoryIn);
    }

    public void recalculateEnchantments() {
        computeEnchantmentData();
        detectAndSendChanges();
    }

    private void computeEnchantmentData() {
        ItemStack itemstack = this.tableInventory.getStackInSlot(0);

        if (itemstack.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                this.enchantLevels[i] = 0;
                this.enchantClue[i] = -1;
                this.worldClue[i] = -1;
            }
            return;
        }

        if (itemstack.isItemEnchanted()) {
            for (int i = 0; i < 3; i++) {
                this.enchantLevels[i] = 0;
                this.enchantClue[i] = -1;
                this.worldClue[i] = -1;
            }
            return;
        }

        int xpSeed = getPlayerEnchantmentSeed();
        float power = countEnchantPower();

        for (int i = 0; i < 3; i++) {
            this.rand.setSeed((long) (xpSeed + i));
            int level = EnchantmentHelper.calcItemStackEnchantability(
                this.rand, i, (int) power, itemstack
            );
            this.enchantClue[i] = -1;
            this.worldClue[i] = -1;

            if (level < i + 1) {
                level = 0;
            }

            level = ForgeEventFactory.onEnchantmentLevelSet(
                world, pos, i, (int) power, itemstack, level
            );
            this.enchantLevels[i] = level;

            if (level > 0) {
                this.rand.setSeed((long) (xpSeed + i));
                boolean allowTreasure = (int) power >= 15;
                List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(
                    this.rand, itemstack, level, allowTreasure
                );
                if (list != null && !list.isEmpty()) {
                    EnchantmentData data = list.get(this.rand.nextInt(list.size()));
                    this.enchantClue[i] = Enchantment.getEnchantmentID(
                        data.enchantment
                    );
                    this.worldClue[i] = data.enchantmentLevel;
                }
            }
        }
    }

    private boolean hasNoCollision(BlockPos checkPos) {
        return world.getBlockState(checkPos).getCollisionBoundingBox(world, checkPos) == null;
    }

    private float countEnchantPower() {
        float power = 0.0F;
        float maxPower = EasyMagicConfig.maxBookshelfCount;

        for (int bx = -1; bx <= 1; bx++) {
            for (int bz = -1; bz <= 1; bz++) {
                if ((bx != 0 || bz != 0)
                    && hasNoCollision(pos.add(bx, 0, bz))
                    && hasNoCollision(pos.add(bx, 1, bz))) {

                    if (bx == 0 || bz == 0) {
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 0, bz * 2)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 1, bz * 2)
                        );
                    }

                    if (bx != 0 && bz != 0) {
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 0, bz)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 0, bz * 2)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx, 0, bz * 2)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 1, bz)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx * 2, 1, bz * 2)
                        );
                        power += ForgeHooks.getEnchantPower(
                            world, pos.add(bx, 1, bz * 2)
                        );
                    }
                }
            }
        }

        return Math.min(power, maxPower);
    }

    private int getPlayerEnchantmentSeed() {
        return this.player.getXPSeed();
    }

    private void setPlayerEnchantmentSeed(int newSeed) {
        ReflectionHelper.setPrivateValue(
            EntityPlayer.class, this.player, newSeed, ENCHANTMENT_SEED_NAMES
        );
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int slotIndex) {
        ItemStack itemstack = this.tableInventory.getStackInSlot(0);
        ItemStack lapisStack = this.tableInventory.getStackInSlot(1);
        int cost = slotIndex + 1;

        if (slotIndex < 0 || slotIndex >= 3) {
            return false;
        }

        if (itemstack.isEmpty()) {
            return false;
        }

        if (this.enchantLevels[slotIndex] <= 0) {
            return false;
        }

        if (lapisStack.isEmpty() || lapisStack.getCount() < cost) {
            return false;
        }

        if (!playerIn.capabilities.isCreativeMode) {
            if (playerIn.experienceLevel < cost
                || playerIn.experienceLevel < this.enchantLevels[slotIndex]) {
                return false;
            }
        }

        int xpSeed = getPlayerEnchantmentSeed();
        float power = countEnchantPower();
        boolean allowTreasure = (int) power >= 15;
        this.rand.setSeed((long) (xpSeed + slotIndex));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(
            this.rand, itemstack, this.enchantLevels[slotIndex], allowTreasure
        );

        if (list == null || list.isEmpty()) {
            return false;
        }

        boolean isBook = itemstack.getItem() instanceof ItemBook;

        if (isBook) {
            itemstack = new ItemStack(Items.ENCHANTED_BOOK);
            this.tableInventory.setInventorySlotContents(0, itemstack);
        }

        for (EnchantmentData data : list) {
            if (isBook) {
                ItemEnchantedBook.addEnchantment(itemstack, data);
            } else {
                itemstack.addEnchantment(data.enchantment, data.enchantmentLevel);
            }
        }

        if (!playerIn.capabilities.isCreativeMode) {
            lapisStack.shrink(cost);
            if (lapisStack.isEmpty()) {
                this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
            }
        }

        this.world.playSound(
            null,
            this.pos,
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.BLOCKS,
            1.0F,
            this.world.rand.nextFloat() * 0.1F + 0.9F
        );

        playerIn.onEnchant(itemstack, cost);

        this.tableInventory.markDirty();
        this.onCraftMatrixChanged(this.tableInventory);
        return true;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        syncToListener(listener);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            boolean changed = false;
            for (int i = 0; i < 3; i++) {
                if (this.enchantLevels[i] != this.lastEnchantLevels[i]) {
                    changed = true;
                    break;
                }
                if (this.enchantClue[i] != this.lastEnchantClue[i]) {
                    changed = true;
                    break;
                }
                if (this.worldClue[i] != this.lastWorldClue[i]) {
                    changed = true;
                    break;
                }
            }

            if (changed) {
                syncToListener(listener);
            }
        }

        System.arraycopy(this.enchantLevels, 0, this.lastEnchantLevels, 0, 3);
        System.arraycopy(this.enchantClue, 0, this.lastEnchantClue, 0, 3);
        System.arraycopy(this.worldClue, 0, this.lastWorldClue, 0, 3);
    }

    private void syncToListener(IContainerListener listener) {
        for (int i = 0; i < 3; i++) {
            listener.sendWindowProperty(this, i, this.enchantLevels[i]);
        }

        if (listener instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(
                new MessageEnchantingData(
                    this.enchantClue,
                    this.worldClue,
                    this.enchantLevels
                ),
                (EntityPlayerMP) listener
            );
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id >= 0 && id < 3) {
            this.enchantLevels[id] = data;
        } else {
            super.updateProgressBar(id, data);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0 || index == 1) {
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() == Items.DYE
                && itemstack1.getMetadata() == 4) {
                if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 38) {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(
            (double) this.pos.getX() + 0.5D,
            (double) this.pos.getY() + 0.5D,
            (double) this.pos.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!this.world.isRemote) {
        }
    }

    public TileEntityEasyEnchantmentTable getTableInventory() {
        return this.tableInventory;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getXpSeed() {
        return getPlayerEnchantmentSeed();
    }

    public int getLapisCount() {
        ItemStack lapis = this.tableInventory.getStackInSlot(1);
        return lapis.isEmpty() ? 0 : lapis.getCount();
    }

    public List<EnchantmentData> getEnchantmentList(int slot) {
        ItemStack itemstack = this.tableInventory.getStackInSlot(0);
        if (itemstack.isEmpty() || this.enchantLevels[slot] <= 0) {
            return java.util.Collections.emptyList();
        }
        int xpSeed = getPlayerEnchantmentSeed();
        float power = countEnchantPower();
        boolean allowTreasure = (int) power >= 15;
        Random r = new Random((long) (xpSeed + slot));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(
            r, itemstack, this.enchantLevels[slot], allowTreasure
        );
        return list != null ? list : java.util.Collections.emptyList();
    }
}
