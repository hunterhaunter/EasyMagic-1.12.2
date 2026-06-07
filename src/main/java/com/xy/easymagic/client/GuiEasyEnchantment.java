package com.xy.easymagic.client;

import com.google.common.collect.Lists;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.container.ContainerEasyEnchantment;
import com.xy.easymagic.network.MessageEnchantItem;
import com.xy.easymagic.network.MessageEnchantingData;
import com.xy.easymagic.network.MessageReroll;
import com.xy.easymagic.network.PacketHandler;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.List;

public class GuiEasyEnchantment extends GuiContainer {

    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE =
        new ResourceLocation("textures/gui/container/enchanting_table.png");

    private final ContainerEasyEnchantment container;
    private final EntityPlayer player;
    private final BlockPos pos;

    private GuiButton rerollButton;

    public GuiEasyEnchantment(
        EntityPlayer player,
        TileEntityEasyEnchantmentTable te,
        World world,
        BlockPos pos
    ) {
        super(new ContainerEasyEnchantment(player, te, world, pos));
        this.container = (ContainerEasyEnchantment) this.inventorySlots;
        this.player = player;
        this.pos = pos;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        if (EasyMagicConfig.rerollEnabled) {
            this.rerollButton = new GuiButtonReroll(
                100, this.guiLeft + 14, this.guiTop + 16
            );
            this.buttonList.add(this.rerollButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            PacketHandler.INSTANCE.sendToServer(new MessageReroll(this.pos));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        if (this.rerollButton != null) {
            this.rerollButton.x = this.guiLeft + 14;
            this.rerollButton.y = this.guiTop + 16;
            GuiButtonReroll btn = (GuiButtonReroll) this.rerollButton;
            btn.setLapisAvailable(this.container.getLapisCount());
            net.minecraft.item.ItemStack tableItem = this.container.getTableInventory().getStackInSlot(0);
            btn.setItemPresent(!tableItem.isEmpty());
            btn.setItemEnchanted(!tableItem.isEmpty() && tableItem.isItemEnchanted());
        }

        EnchantmentNameParts.getInstance().reseedRandomGenerator((long) this.container.getXpSeed());
        int lapisCount = this.container.getLapisCount();

        for (int l = 0; l < 3; ++l) {
            int i1 = i + 60;
            int j1 = i1 + 20;
            this.zLevel = 0.0F;
            this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
            int k1 = this.container.enchantLevels[l];
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (k1 == 0) {
                this.drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 185, 108, 19);
            } else {
                String s = "" + k1;
                int l1 = 86 - this.fontRenderer.getStringWidth(s);
                String s1 = EnchantmentNameParts.getInstance().generateNewRandomName(
                    this.fontRenderer, l1
                );
                net.minecraft.client.gui.FontRenderer fontrenderer =
                    this.mc.standardGalacticFontRenderer;
                int i2 = 6839882;

                boolean creative = this.mc.player.capabilities.isCreativeMode;
                if (lapisCount < l + 1
                    || (!creative && this.mc.player.experienceLevel < k1)
                    || this.container.enchantClue[l] == -1) {
                    this.drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 185, 108, 19);
                    this.drawTexturedModalRect(i1 + 1, j + 15 + 19 * l, 16 * l, 239, 16, 16);
                    fontrenderer.drawSplitString(s1, j1, j + 16 + 19 * l, l1, (i2 & 16711422) >> 1);
                    i2 = 4226832;
                } else {
                    int j2 = mouseX - (i + 60);
                    int k2 = mouseY - (j + 14 + 19 * l);

                    if (j2 >= 0 && k2 >= 0 && j2 < 108 && k2 < 19) {
                        this.drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 204, 108, 19);
                        i2 = 16777088;
                    } else {
                        this.drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 166, 108, 19);
                    }

                    this.drawTexturedModalRect(i1 + 1, j + 15 + 19 * l, 16 * l, 223, 16, 16);
                    fontrenderer.drawSplitString(s1, j1, j + 16 + 19 * l, l1, i2);
                    i2 = 8453920;
                }

                fontrenderer = this.mc.fontRenderer;
                fontrenderer.drawStringWithShadow(
                    s,
                    (float) (j1 + 86 - fontrenderer.getStringWidth(s)),
                    (float) (j + 16 + 19 * l + 7),
                    i2
                );
            }
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        partialTicks = this.mc.getTickLength();
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        int lapisCount = this.container.getLapisCount();

        for (int slot = 0; slot < 3; ++slot) {
            int k = this.container.enchantLevels[slot];
            Enchantment enchantment = Enchantment.getEnchantmentByID(
                this.container.enchantClue[slot]
            );
            int l = this.container.worldClue[slot];
            int i1 = slot + 1;

            if (this.isPointInRegion(60, 14 + 19 * slot, 108, 17, mouseX, mouseY) && k > 0) {
                List<String> list = Lists.newArrayList();

                if (EasyMagicConfig.showEnchantmentHints && enchantment != null) {
                    int hintCount = EasyMagicConfig.enchantmentHintCount;
                    if (hintCount == 1) {
                        list.add("" + TextFormatting.WHITE + TextFormatting.ITALIC
                            + I18n.format("container.enchant.clue",
                                enchantment.getTranslatedName(l)));
                    } else {
                        List<EnchantmentData> fullList = this.container.getEnchantmentList(slot);
                        int show = hintCount < 0 ? fullList.size() : Math.min(hintCount, fullList.size());
                        for (int ei = 0; ei < show; ei++) {
                            EnchantmentData ed = fullList.get(ei);
                            list.add("" + TextFormatting.WHITE + TextFormatting.ITALIC
                                + ed.enchantment.getTranslatedName(ed.enchantmentLevel));
                        }
                        if (show == 0) {
                            list.add("" + TextFormatting.WHITE + TextFormatting.ITALIC
                                + I18n.format("container.enchant.clue",
                                    enchantment.getTranslatedName(l)));
                        }
                    }
                } else {
                    list.add("" + TextFormatting.WHITE + TextFormatting.ITALIC
                        + I18n.format("container.enchant.clue",
                            enchantment == null ? "" : enchantment.getTranslatedName(l)));
                }

                if (enchantment == null) {
                    list.add("");
                    list.add(TextFormatting.RED
                        + I18n.format("forge.container.enchant.limitedEnchantability"));
                } else {
                    list.add("");

                    if (!this.mc.player.capabilities.isCreativeMode
                        && this.mc.player.experienceLevel < k) {
                        list.add(TextFormatting.RED + I18n.format(
                            "container.enchant.level.requirement",
                            this.container.enchantLevels[slot]
                        ));
                    } else {
                        String s;

                        if (i1 == 1) {
                            s = I18n.format("container.enchant.lapis.one");
                        } else {
                            s = I18n.format("container.enchant.lapis.many", i1);
                        }

                        TextFormatting textformatting =
                            lapisCount >= i1 ? TextFormatting.GRAY : TextFormatting.RED;
                        list.add(textformatting + "" + s);

                        if (i1 == 1) {
                            s = I18n.format("container.enchant.level.one");
                        } else {
                            s = I18n.format("container.enchant.level.many", i1);
                        }

                        list.add(TextFormatting.GRAY + "" + s);
                    }
                }

                this.drawHoveringText(list, mouseX, mouseY);
                break;
            }
        }

        if (EasyMagicConfig.rerollEnabled && this.rerollButton != null
            && this.rerollButton.isMouseOver()) {
            List<String> tooltip = Lists.newArrayList();
            tooltip.add(TextFormatting.GRAY + I18n.format("easymagic.reroll.tooltip"));

            if (!this.mc.player.capabilities.isCreativeMode) {
                List<String> costLines = Lists.newArrayList();

                int lapisCost = EasyMagicConfig.rerollLapisCost;
                if (lapisCost >= 1) {
                    int lapisAvail = this.container.getLapisCount();
                    TextFormatting color = lapisAvail >= lapisCost
                        ? TextFormatting.GRAY : TextFormatting.RED;
                    String lapisText = lapisCost == 1
                        ? I18n.format("easymagic.reroll.lapis.one")
                        : I18n.format("easymagic.reroll.lapis.many", lapisCost);
                    costLines.add(color + lapisText);
                }

                int xpCost = EasyMagicConfig.rerollXpCost;
                if (xpCost >= 1) {
                    int xpAvail = this.mc.player.experienceTotal;
                    TextFormatting color = xpAvail >= xpCost
                        ? TextFormatting.GRAY : TextFormatting.RED;
                    String xpText = xpCost == 1
                        ? I18n.format("easymagic.reroll.xp.one")
                        : I18n.format("easymagic.reroll.xp.many", xpCost);
                    costLines.add(color + xpText);
                }

                if (!costLines.isEmpty()) {
                    tooltip.add("");
                    tooltip.addAll(costLines);
                }
            }

            this.drawHoveringText(tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.enchant"), 12, 5, 4210752);
        this.fontRenderer.drawString(
            I18n.format("container.inventory"),
            8,
            this.ySize - 96 + 2,
            4210752
        );
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) return;

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        for (int k = 0; k < 3; ++k) {
            int l = mouseX - (i + 60);
            int i1 = mouseY - (j + 14 + 19 * k);

            if (l >= 0 && i1 >= 0 && l < 108 && i1 < 19
                && this.container.enchantLevels[k] > 0) {

                int requiredLevel = k + 1;
                int lapisCount2 = this.container.getLapisCount();
                boolean hasLapis = lapisCount2 >= requiredLevel;
                boolean hasXp = this.mc.player.capabilities.isCreativeMode
                    || this.mc.player.experienceLevel >= this.container.enchantLevels[k];

                if (hasLapis && hasXp) {
                    PacketHandler.INSTANCE.sendToServer(
                        new MessageEnchantItem(this.pos, k)
                    );
                }
            }
        }
    }

    public void updateEnchantmentData(MessageEnchantingData message) {
        System.arraycopy(message.getEnchantClue(), 0, this.container.enchantClue, 0, 3);
        System.arraycopy(message.getWorldClue(), 0, this.container.worldClue, 0, 3);
        System.arraycopy(message.getEnchantLevels(), 0, this.container.enchantLevels, 0, 3);
    }
}
