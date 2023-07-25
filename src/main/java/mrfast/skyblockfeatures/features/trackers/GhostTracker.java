package mrfast.skyblockfeatures.features.trackers;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.events.SecondPassedEvent;
import mrfast.skyblockfeatures.utils.NumberUtil;
import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mrfast.skyblockfeatures.gui.components.Point;

public class GhostTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static int Volta = 0;
    static int Sorrow = 0;
    static int Plasma = 0;
    static int Boots = 0;

    static boolean hidden = true;
    static int kills = 0;
    static int oldKills = 0;
    static int seconds = 0;
    static int totalSeconds = 0;
    
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || !SkyblockFeatures.config.ghostTracker) return;

        try {
            seconds = 0;
            kills = 0;
            oldKills = 0;
            hidden = true;
            totalSeconds = 0;
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || !SkyblockFeatures.config.ghostTracker) return;
        
        if(oldKills == 0) {
            oldKills = kills;
        }
        if(!hidden) {
            totalSeconds++;
        }
        if(seconds >= 60) {
            if(oldKills == kills) {
                hidden = true;
                totalSeconds=0;
            }
            oldKills = kills;
            seconds = 0;
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || !SkyblockFeatures.config.ghostTracker) return;
        Entity entity = event.entity;
        if(entity instanceof EntityCreeper) {
            if(Utils.GetMC().thePlayer.getDistanceToEntity(entity) < 10) {
                hidden = false;
                kills++;
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(SecondPassedEvent event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || !SkyblockFeatures.config.ghostTracker) return;
        for(int i=0;i<Utils.GetMC().thePlayer.inventory.mainInventory.length;i++) {
            if(Utils.GetMC().thePlayer.inventory.mainInventory[i] != null) {
                if(i == 0) {
                    Volta = 0;
                    Sorrow = 0;
                    Plasma = 0;
                    Boots = 0;
                }
                try {
                    ItemStack stack = Utils.GetMC().thePlayer.inventory.mainInventory[i];
                    String name = Utils.cleanColor(stack.getDisplayName());
        
                    if(PricingData.getIdentifier(stack) != null) {
                        if(name.contains("Volta")) {
                            Volta+=stack.stackSize;
                        }
                        if(name.contains("Sorrow")) {
                            Sorrow+=stack.stackSize;
                        }
                        if(name.contains("Plasma")) {
                            Plasma+=stack.stackSize;
                        }
                        if(name.contains("Ghostly")) {
                            Boots+=stack.stackSize;
                        }
                    }
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
        }
    }
    static {
        new AutomatonTrackerGUI();
    }

    static String display = "";
    public static class AutomatonTrackerGUI extends UIElement {
        public AutomatonTrackerGUI() {
            super("Ghost Tracker", new Point(0.45052084f, 0.86944443f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null && !hidden) {
                if(!SkyblockInfo.getInstance().location.contains("Mist")) return;
                String[] lines = {
                    ChatFormatting.GREEN+"Time Elapsed: §r"+Utils.secondsToTime(totalSeconds),
                    ChatFormatting.GREEN+"Ghosts Killed: §r"+NumberUtil.nf.format(kills),
                    ChatFormatting.BLUE+"Volta: §r"+Volta,
                    ChatFormatting.BLUE+"Sorrow: §r"+Sorrow,
                    ChatFormatting.GOLD+"Plasma: §r"+Plasma,
                    ChatFormatting.LIGHT_PURPLE+"Ghostly Boots: §r"+Boots,
                    ChatFormatting.YELLOW+"Ghosts/Sorrow: §r"+(Sorrow>0?Math.round(kills/Sorrow):"Undefined"),
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                    lineCount++;
                }
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String[] lines = {
                ChatFormatting.GREEN+"Time Elapsed: §r27m 3s",
                ChatFormatting.GREEN+"Ghosts Killed: §r203",
                ChatFormatting.BLUE+"Volta: §r3",
                ChatFormatting.BLUE+"Sorrow: §r1",
                ChatFormatting.GOLD+"Plasma: §r2",
                ChatFormatting.LIGHT_PURPLE+"Ghostly Boots: §r1",
                ChatFormatting.YELLOW+"Ghosts/Sorrow: §r129",
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.ghostTracker;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*7;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Electron Transmitter: 10");
        }
    }
}
