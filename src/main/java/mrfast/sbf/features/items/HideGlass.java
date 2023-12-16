package mrfast.sbf.features.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import mrfast.sbf.utils.GuiUtils;
import org.lwjgl.input.Keyboard;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.commands.TerminalCommand;
import mrfast.sbf.commands.debugCommand;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HideGlass {
    public static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.timestamps || event.type == 2) return;
        String timestamp = new SimpleDateFormat("hh:mm").format(new Date());
        event.message = new ChatComponentText("")
                .appendText(ChatFormatting.DARK_GRAY+"["+timestamp+"] ")
                .appendSibling(event.message);
    }

    @SubscribeEvent
    public void onTooltipLow(ItemTooltipEvent event) {
        if(Utils.inSkyblock && SkyblockFeatures.config.showSkyblockID) {
            for(int i = 0; i < event.toolTip.size(); i++) {
                String line = Utils.cleanColor(event.toolTip.get(i));
                if(line.contains("minecraft:")) {
                    event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"ID: "+ItemUtils.getSkyBlockItemID(event.itemStack));
                    if(Utils.isDeveloper()) {
                        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = event.itemStack.getTagCompound();
                            if(tag!=null) {
                                event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"DATA: "+ debugCommand.prettyPrintNBT(tag));
                            }
                        } else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = ItemUtils.getExtraAttributes(event.itemStack);
                            if(tag!=null) {
                                event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"DATA: "+ debugCommand.prettyPrintNBT(tag));
                            }
                        }
                    }
                    break;
                }
            }
        }

        if(Utils.inSkyblock && isEmptyGlassPane(event.itemStack)) {
            event.toolTip.clear();
        }
    }

    public static boolean isEmptyGlassPane(ItemStack itemStack) {
        return itemStack != null && (itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                || itemStack.getItem() == Item.getItemFromBlock(Blocks.glass_pane)) && itemStack.hasDisplayName() && Utils.cleanColor(itemStack.getDisplayName().trim()).isEmpty();
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        TerminalCommand.start = 0;
        TerminalCommand.mazeIndex = 1;
    }
    
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(Utils.isDeveloper()) {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                event.setCanceled(true);
                System.out.println("-====================================-");
                for (String itemLore : ItemUtils.getItemLore(event.slot.getStack())) {
                    System.out.println(itemLore);
                }
                System.out.println("-====================================-");
            }
        }
        if(event.inventoryName.contains("✯")) {
            TerminalCommand.handleTerminalClick(event);
        }
    }

    @SubscribeEvent
    public void onTitleDrawn(TitleDrawnEvent event) {
        if(!(event.gui instanceof GuiChest)) return;
        GuiChest gui = (GuiChest) event.gui;
        ContainerChest chest = (ContainerChest) gui.inventorySlots;
        IInventory inv = chest.getLowerChestInventory();
        String chestName = inv.getDisplayName().getUnformattedText().trim();
        if(chestName.contains("✯")) {
            if(TerminalCommand.start!=0) Utils.GetMC().fontRendererObj.drawStringWithShadow(chestName+" "+(Utils.round((System.currentTimeMillis() - TerminalCommand.start)/1000d,2))+"s", 8, 6, 0);
            else Utils.GetMC().fontRendererObj.drawStringWithShadow(chestName, 8, 6, 0);
        }
    }

    // Debug Mode to see all the slots ids
    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (event.gui instanceof GuiChest ) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            if(Utils.isDeveloper()) {
                if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    for(int i=0;i<chest.inventorySlots.size();i++) {
                        
                        int x = chest.inventorySlots.get(i).xDisplayPosition;
                        int y = chest.inventorySlots.get(i).yDisplayPosition;
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0, 0, 700);
                        GuiUtils.drawText(ChatFormatting.GREEN+""+i,x+6, y+6, GuiUtils.TextStyle.BLACK_OUTLINE);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
}
