package mrfast.skyblockfeatures.features.actionBar;

import net.minecraft.client.Minecraft;
import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.utils.Utils;
import mrfast.skyblockfeatures.gui.components.Point;


public class SpeedDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new JerryTimerGUI();
    }

    static String display = "123%";

    public static String getSpeed() {
        String text = "";
        String walkSpeed = String.valueOf(Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed() * 1000);
        text = walkSpeed.substring(0, Math.min(walkSpeed.length(), 3));
        if (text.endsWith(".")) text = text.substring(0, text.indexOf('.')); //remove trailing periods
        text += "%";
        return text;
    }
    public static class JerryTimerGUI extends UIElement {
        public JerryTimerGUI() {
            super("Speed Display", new Point(0.375f, 0.975f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                Utils.drawTextWithStyle(getSpeed(), 0, 0, 0xFFFFFF);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle("123%", 0, 0, 0xFFFFFF);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.SpeedDisplay;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
