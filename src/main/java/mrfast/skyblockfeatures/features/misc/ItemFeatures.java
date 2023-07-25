package mrfast.skyblockfeatures.features.misc;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.utils.ItemUtil;
import mrfast.skyblockfeatures.utils.NumberUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemFeatures {
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.inSkyblock) return;

        ItemStack item = event.itemStack;
        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
        String itemId = ItemUtil.getSkyBlockItemID(extraAttr);
        String itemUUID = ItemUtil.getItemUUID(item);
        if (itemId != null) {
            if (SkyblockFeatures.config.egg) {
                NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

                if (extraAttributes != null) {
                    if (extraAttributes.hasKey("blocks_walked")) {
                        int walked = extraAttributes.getInteger("blocks_walked");
                        event.toolTip.add("§e" + NumberUtil.nf.format(walked)+" blocks walked");
                    }
                }
            }
            if(AuctionFeatures.items.containsKey(item) && SkyblockFeatures.config.showPricePaid) {
                long price = Math.round(AuctionFeatures.items.get(item));
                String color = price>0?ChatFormatting.GREEN+"":ChatFormatting.RED+"";
                event.toolTip.add("§6BIN Flip Profit: "+color+NumberUtil.nf.format(price));
            }
        }
        if (itemId != null) {
            if(SkyblockFeatures.config.showPriceInfoOnShift) {
                if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    event.toolTip.add("§e§l[SHIFT To Reveal Info]");
                    return;
                }
            }
            if(itemUUID != null) {
                if(AuctionFeatures.pricePaidMap.containsKey(itemUUID)) {
                    event.toolTip.add("§bPrice Paid: §d" + NumberUtil.nf.format(AuctionFeatures.pricePaidMap.get(itemUUID)));
                }
            }
            String auctionIdentifier = PricingData.getIdentifier(item);
            if (auctionIdentifier != null && item!=null) {
                JsonObject auctionData = PricingData.getItemAuctionInfo(auctionIdentifier);
                if (SkyblockFeatures.config.showSalesPerDay && auctionData!=null) {
                    event.toolTip.add("§bSales Per Day: §e" + NumberUtil.nf.format(auctionData.get("sales").getAsInt()));
                }
                
                Double valuePer = PricingData.lowestBINs.get(auctionIdentifier);

                if (SkyblockFeatures.config.showEstimatedPrice && valuePer!=null) {
                    Integer total = (int) ItemUtil.getEstimatedItemValue(item);//Math.floor(valuePer+starValue+enchantValue) * item.stackSize;
                    event.toolTip.add("§bEstimated Price: §6" + NumberUtil.nf.format(total*item.stackSize));
                }

                if (SkyblockFeatures.config.showLowestBINPrice && valuePer!=null) {
                    String total = NumberUtil.nf.format(valuePer * item.stackSize);
                    event.toolTip.add("§bLowest BIN Price: §a" + total + (item.stackSize > 1 ? " §7(" + NumberUtil.nf.format(Math.round(valuePer)) + " each§7)" : ""));
                }
                
                valuePer = PricingData.bazaarPrices.get(auctionIdentifier);
                if (SkyblockFeatures.config.showBazaarPrice && valuePer != null) {
                    String total = NumberUtil.nf.format(valuePer * item.stackSize);
                    event.toolTip.add("§bLowest Bazaar Price: §a" + total + (item.stackSize > 1 ? " §7(" + NumberUtil.nf.format(Math.round(valuePer)) + " each§7)" : ""));
                }

                Double avgValuePer = PricingData.averageLowestBINs.get(auctionIdentifier);
                if (SkyblockFeatures.config.showAvgLowestBINPrice && avgValuePer!=null) {
                    String total = NumberUtil.nf.format(avgValuePer * item.stackSize);
                    event.toolTip.add("§bAverage BIN Price: §2" + total + (item.stackSize > 1 ? " §7(" + NumberUtil.nf.format(Math.round(avgValuePer)) + " each§7)" : ""));
                }
            }

            if (SkyblockFeatures.config.showNPCSellPrice && item!=null) {
                Integer valuePer = PricingData.npcSellPrices.get(itemId);
                if (valuePer != null) event.toolTip.add("§bNPC Sell Value: §3" + NumberUtil.nf.format(valuePer * item.stackSize) + (item.stackSize > 1 ? " §7(" + NumberUtil.nf.format(valuePer) + " each§7)" : ""));
            }
        }
    }
}
