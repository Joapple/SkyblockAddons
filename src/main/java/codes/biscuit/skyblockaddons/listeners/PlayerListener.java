package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.ButtonSlider;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.Gui.icons;

public class PlayerListener {

    public final static ItemStack BONE = new ItemStack(Item.getItemById(352));
    public final static ResourceLocation MANA_BARS = new ResourceLocation("skyblockaddons", "manabars.png");

    private boolean sentUpdate = false;
    private boolean predictMana = false;
    private long lastWorldJoin = -1;
    private int mana = 0;
    private int maxMana = 100;
    private boolean openGUI = false;
    private boolean fullInventoryWarning = false;
    private boolean bossWarning = false;
    private long lastBoss = -1;
    private int soundTick = 1;
    private int manaTick = 1;
//    private Map<Long, String> spawnLog = new HashMap<>();

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = System.currentTimeMillis();
            bossWarning = false;
            lastBoss = -1;
            soundTick = 1;
            manaTick = 1;
        }
    }

    @SubscribeEvent()
    public void onRenderBossWarning(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.TEXT) { // Render a title-like warning.
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledresolution = e.resolution;
            int i = scaledresolution.getScaledWidth();
            if (bossWarning) {
                int j = scaledresolution.getScaledHeight();
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                String text;
                text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting() + "MagmaCube Boss!";
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
                GlStateManager.popMatrix();
//            GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
            if (fullInventoryWarning && !main.getConfigValues().getDisabledFeatures().contains(Feature.FULL_INVENTORY_WARNING)) {
                int j = scaledresolution.getScaledHeight();
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                String text;
                text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting() + "Full Inventory!";
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
            if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_BOSS_BAR)) {
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityArmorStand) {
                        String name = entity.getDisplayName().getFormattedText();
                        if (name.contains("Magma Cube Boss ")) {
                            name = name.split(Pattern.quote("Magma Cube Boss "))[1];
                            mc.getTextureManager().bindTexture(icons);
                            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                            GlStateManager.enableBlend();
                            int j = 182;
                            int k = i / 2 - j / 2;
                            int health = 1;
                            int l = (int) (health * (float) (j + 1));
                            int i1 = 12;
                            mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);
                            mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);

                            if (l > 0) {
                                mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 79, l, 5);
                            }
                            mc.ingameGUI.getFontRenderer().drawStringWithShadow(name, (float) (i / 2 - mc.ingameGUI.getFontRenderer().getStringWidth(name) / 2), (float) (i1 - 10), 16777215);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            mc.getTextureManager().bindTexture(icons);
                            GlStateManager.disableBlend();
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent()
    public void onChatReceive(ClientChatReceivedEvent e) {
        if (main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF) {
            String message = e.message.getUnformattedText();
            if (e.type == 2) {
                if (message.contains("\u270E Mana")) {
                    String[] manaSplit = message.split(Pattern.quote("\u270E Mana"));
                    if (manaSplit.length > 1) {
                        if (manaSplit[0].contains(EnumChatFormatting.AQUA.toString())) {
                            message = manaSplit[0].split(Pattern.quote(EnumChatFormatting.AQUA.toString()))[1];
                            manaSplit = message.split(Pattern.quote("/"));
                            mana = Integer.parseInt(manaSplit[0]);
                            maxMana = Integer.parseInt(manaSplit[1]);
                            e.message = new ChatComponentText(e.message.getUnformattedText().split(EnumChatFormatting.AQUA.toString())[0].trim());
                            predictMana = false;
                            return;
                        }
                    }
                }
                predictMana = true;
            } else {
                if (predictMana && message.startsWith("Used ") && message.endsWith("Mana)")) {
                    int mana = Integer.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                    this.mana -= mana;
                }
            }
        }
    }

    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if (!main.isUsingLabymod() && e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE && main.getUtils().isOnSkyblock()) {
            renderOverlays(e.resolution);
        }
    }

    @SubscribeEvent()
    public void onRenderLabyMod(RenderGameOverlayEvent e) {
        if (main.isUsingLabymod() && main.getUtils().isOnSkyblock()) {
            renderOverlays(e.resolution);
        }
    }

    private void renderOverlays(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP);
        float scaleMultiplier = 1F/scale;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        if (main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF && !(mc.currentScreen instanceof LocationEditGui)) {
            mc.getTextureManager().bindTexture(MANA_BARS);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();

            short barWidth = 92;
            if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR
                    || main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
                float manaFill = (float) mana / maxMana;
                if (manaFill > 1) manaFill = 1;
                int left = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth()) + 14;
                int filled = (int) (manaFill * barWidth);
                int top = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight()) + 10;
                // mc.ingameGUI.drawTexturedModalRect(left, top, 10, 84, barWidth, 5);
                int textureY = main.getConfigValues().getColor(Feature.MANA_BAR_COLOR).ordinal()*10;
                mc.ingameGUI.drawTexturedModalRect(left*scaleMultiplier-60, top*scaleMultiplier-10, 0, textureY, barWidth, 5);
                if (filled > 0) {
//                        mc.ingameGUI.drawTexturedModalRect(left, top, 10, 89, filled, 5);
                    mc.ingameGUI.drawTexturedModalRect(left*scaleMultiplier-60, top*scaleMultiplier-10, 0, textureY+5, filled, 5);
                }
            }
            if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.TEXT
                    || main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
                int color = main.getConfigValues().getColor(Feature.MANA_TEXT_COLOR).getColor(255);
                String text = mana + "/" + maxMana;
                int x = (int) (main.getConfigValues().getManaTextX() * sr.getScaledWidth()) + 60 - mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
                int y = (int) (main.getConfigValues().getManaTextY() * sr.getScaledHeight()) + 4;
                x+=25;
                y+=10;
                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60+1, (int)(y*scaleMultiplier)-10, 0);
                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60-1, (int)(y*scaleMultiplier)-10, 0);
                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)+1-10, 0);
                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-1-10, 0);
                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
//                int x = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth()) + 60 - mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
//                int y = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight()) + 4;
////                x+=60;
////                y+=10;
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x-60*scaleMultiplier) + 1, (int)(y*scaleMultiplier)-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60 - 1, (int)(y*scaleMultiplier)-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)+ 1-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier) - 1-10, 0);
//                mc.ingameGUI.getFontRenderer().drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        if ((!main.getConfigValues().getDisabledFeatures().contains(Feature.SKELETON_BAR))
                && !(mc.currentScreen instanceof LocationEditGui)) {
            int width = (int)(main.getConfigValues().getSkeletonBarX()*sr.getScaledWidth());
            int height = (int)(main.getConfigValues().getSkeletonBarY()*sr.getScaledHeight());
            int bones = 0;
            for (Entity listEntity : mc.theWorld.loadedEntityList) {
                if (listEntity instanceof EntityItem &&
                        listEntity.ridingEntity instanceof EntityZombie && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 6) {
                    bones++;
                }
            }
            if (bones > 3) bones = 3;
            for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
                mc.getRenderItem().renderItemIntoGUI(BONE, (int)((width+boneCounter*15*scale)*scaleMultiplier), (int)((height+2)*scaleMultiplier));
            }
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (main.getUtils().isOnSkyblock() && !main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_FOOD_ARMOR_BAR)) {
                GuiIngameForge.renderFood = false;
                GuiIngameForge.renderArmor = false;
            }
        }
    }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack heldItem = e.entityPlayer.getHeldItem();
            if (e.entityPlayer == mc.thePlayer && heldItem != null) {
                if (heldItem.getItem().equals(Items.blaze_rod) && heldItem.isItemEnchanted() && main.getUtils().isOnIsland()) {
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent()
    public void onTickMana(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            manaTick++;
            if (manaTick == 20) {
                if (predictMana) {
                    mana += (maxMana/50);
                    if (mana>maxMana) mana = maxMana;
                }
            } else if (manaTick % 5 == 0) {
                main.getUtils().checkIfInventoryIsFull();
            } else if (manaTick > 20) {
                main.getUtils().checkIfOnSkyblockAndIsland();
                Minecraft mc = Minecraft.getMinecraft();
                if (!sentUpdate && mc != null && mc.thePlayer != null && mc.theWorld != null) {
                    main.getUtils().checkUpdates();
                    sentUpdate = true;
                }
                manaTick = 1;
            }
        }
    }

    @SubscribeEvent()
    public void onTickMagmaBossChecker(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && soundTick % 5 == 0) {
                    for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                        if (entity instanceof EntityMagmaCube) {
                            EntitySlime magma = (EntitySlime) entity;
                            int size = magma.getSlimeSize();
                            if (size > 10) { // Find a big magma boss
                                lastBoss = System.currentTimeMillis();
                                bossWarning = true; // Enable warning and disable again in four seconds.
                                soundTick = 16; // so the sound plays instantly
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        bossWarning = false;
                                    }
                                }, main.getConfigValues().getWarningSeconds()*1000); // 4 second warning.
//                                logServer(mc);
                            }
                        }
                    }
                }
                if (bossWarning && soundTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                    mc.thePlayer.playSound("random.orb", 1, 0.5F);
                }
            }
            soundTick++;
            if (soundTick > 20) {
                soundTick = 1;
            }
        }
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (isOpenGUI()) {
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main));
            setOpenGUI(false);
        }
    }

    public boolean isBossWarning() {
        return bossWarning;
    }

    //    private void logServer(Minecraft mc) { // for magma boss logs
//        if (mc.ingameGUI.getTabList().header != null) {
//            List<IChatComponent> siblings = mc.ingameGUI.getTabList().header.getSiblings(); // Bring back AT if doing this
//            if (siblings.size() > 2) {
//                String dateAndServer = siblings.get(siblings.size() - 3).getUnformattedText();
//                spawnLog.put(System.currentTimeMillis(), dateAndServer.split(Pattern.quote("  "))[1]);
//            }
//        }
//    }

    public void setOpenGUI(boolean openGUI) {
        this.openGUI = openGUI;
    }

    private boolean isOpenGUI() {
        return openGUI;
    }

    public void setFullInventoryWarning(boolean fullInventoryWarning) {
        this.fullInventoryWarning = fullInventoryWarning;
    }

    public boolean isFullInventoryWarning() {
        return fullInventoryWarning;
    }

    public long getLastWorldJoin() {
        return lastWorldJoin;
    }
}