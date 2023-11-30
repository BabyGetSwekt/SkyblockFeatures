package mrfast.sbf.features.overlays;

import java.awt.Color;
import java.util.*;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GiftTracker {
    public static ArrayList<Entity> saintJerryGifts = new ArrayList<Entity>();

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.target instanceof EntityArmorStand && ((EntityArmorStand) event.target).getCurrentArmor(3) != null && ((EntityArmorStand) event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBmNTM5ODUxMGIxYTA1YWZjNWIyMDFlYWQ4YmZjNTgzZTU3ZDcyMDJmNTE5M2IwYjc2MWZjYmQwYWUyIn19fQ=") && !saintJerryGifts.contains(event.target)) {
            // If this is false its a player gift
            if (event.target.lastTickPosX == event.target.posX && event.target.lastTickPosY == event.target.posY && event.target.lastTickPosZ == event.target.posZ) {
                saintJerryGifts.add(event.target);
            }
        }
    }

    private static class Gift {
        Boolean giftToSelf = false;
        Entity entity;
        Entity toEntity;
        Entity fromEntity;
    }

    private static HashMap<Entity, Gift> gifts = new HashMap<>();

    @SubscribeEvent
    public void onRenderEntity(CheckRenderEntityEvent event) {
        if (event.entity instanceof EntityArmorStand) {
            for (Gift gift : gifts.values()) {
                if (gift.fromEntity.getCustomNameTag().contains(Utils.GetMC().thePlayer.getName())) continue;
                boolean isRecipient = gift.toEntity.getUniqueID().equals(event.entity.getUniqueID());
                boolean isFrom = gift.fromEntity.getUniqueID().equals(event.entity.getUniqueID());
                boolean isGift = gift.entity.getUniqueID().equals(event.entity.getUniqueID());

                if ((isFrom || isRecipient || isGift) && !gift.giftToSelf && SkyblockFeatures.config.hideOtherGifts) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inSkyblock || Utils.GetMC().theWorld == null || !SkyblockFeatures.config.hideGiftParticles) return;

        if (event.packet instanceof S2APacketParticles) {
            double x = ((S2APacketParticles) event.packet).getXCoordinate();
            double y = ((S2APacketParticles) event.packet).getYCoordinate();
            double z = ((S2APacketParticles) event.packet).getZCoordinate();
            for (Gift gift : gifts.values()) {
                if (gift.entity.getPositionVector().distanceTo(new Vec3(x, y, z)) < 3) {
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Utils.GetMC().theWorld == null) return;
        ;
        for (Map.Entry<Entity, Gift> entry : new HashMap<>(gifts).entrySet()) {
            if (!entry.getKey().isEntityAlive()) {
                gifts.remove(entry.getKey());
            }
        }
        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) continue;
            ;
            if (isPlayerPresent((EntityArmorStand) entity)) {
                Gift gift = new Gift();
                gift.entity = entity;
                gift.toEntity = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId() + 1);
                gift.fromEntity = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId() + 2);
                if (gift.toEntity == null || gift.fromEntity == null) continue;
                if (!gift.toEntity.hasCustomName() || !gift.fromEntity.hasCustomName()) continue;

                if (gift.toEntity.getCustomNameTag().contains("CLICK TO OPEN")) {
                    gift.giftToSelf = true;
                }
                if (!gift.toEntity.getCustomNameTag().contains("To:") || !gift.fromEntity.getCustomNameTag().contains("From:")) {
                    if (!gift.giftToSelf) continue;
                }

                if (gift.toEntity.getDistanceToEntity(gift.entity) > 1 || gift.fromEntity.getDistanceToEntity(gift.entity) > 1 || gift.entity.getDistanceToEntity(Utils.GetMC().thePlayer) > 30) {
                    Utils.SendMessage("Dist req " + gift.toEntity.getDistanceToEntity(gift.entity) + " " + gift.fromEntity.getDistanceToEntity(gift.entity));
                    continue;
                }

                gifts.put(entity, gift);
            }
        }
    }

    public boolean isPlayerPresent(EntityArmorStand entity) {
        if (gifts.containsKey(entity)) return true;
        if (entity.getCurrentArmor(3) == null) return false;
        if (ItemUtils.getSkyBlockItemID(entity.getCurrentArmor(3)) != null) {
            return ItemUtils.getSkyBlockItemID(entity.getCurrentArmor(3)).contains("_GIFT");
        }
        boolean isMoving = (entity.lastTickPosX != entity.posX || entity.lastTickPosY != entity.posY || entity.lastTickPosZ != entity.posZ || entity.prevRotationPitch != entity.rotationPitch || entity.prevRotationYaw != entity.rotationYaw);
        return isMoving;
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || Utils.inDungeons || !Utils.inSkyblock) return;
        boolean inGlacialCave = SkyblockInfo.localLocation.contains("Glacial");

        if (SkyblockFeatures.config.highlightSelfGifts) {
            for (Gift gift : gifts.values()) {
                if (!gift.giftToSelf) continue;

                highlightBlock(SkyblockFeatures.config.selfGiftHighlightColor, gift.entity.posX - 0.5, gift.entity.posY + 1.5, gift.entity.posZ - 0.5, 1.0D, event.partialTicks);
            }
        }
        if (SkyblockFeatures.config.icecaveHighlightWalls) GlStateManager.disableDepth();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (SkyblockFeatures.config.presentWaypoints && entity instanceof EntityArmorStand && !inGlacialCave && ((EntityArmorStand) entity).getCurrentArmor(3) != null && ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBmNTM5ODUxMGIxYTA1YWZjNWIyMDFlYWQ4YmZjNTgzZTU3ZDcyMDJmNTE5M2IwYjc2MWZjYmQwYWUyIn19fQ=")) {
                boolean isPlayerGift = false;
                for (Entity otherEntity : mc.theWorld.loadedEntityList) {
                    if (otherEntity instanceof EntityArmorStand && otherEntity.getDistanceToEntity(entity) < 0.5 && otherEntity.getName().contains("From: ")) {
                        isPlayerGift = true;
                    }
                }
                if (isPlayerPresent((EntityArmorStand) entity)) {
                    isPlayerGift = true;
                }
                if (!saintJerryGifts.contains(entity) && !isPlayerGift) {
                    highlightBlock(SkyblockFeatures.config.presentWaypointsColor, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                }
            }
            if (inGlacialCave && SkyblockFeatures.config.icecaveHighlight) {
                Block blockState = mc.theWorld.getBlockState(entity.getPosition()).getBlock();
                if (SkyblockFeatures.config.icecaveHighlight && (blockState instanceof BlockIce || blockState instanceof BlockPackedIce) && entity instanceof EntityArmorStand && ((EntityArmorStand) entity).getCurrentArmor(3) != null) {
                    String itemName = ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    Vec3 StringPos = new Vec3(entity.posX, entity.posY + 3, entity.posZ);

                    // White gift
                    if (itemName.contains("White Gift")) {
                        highlightBlock(Color.white, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.WHITE + "White Gift", event.partialTicks);
                    }
                    // Green Gift
                    else if (itemName.contains("Green Gift")) {
                        highlightBlock(Color.green, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.GREEN + "Green Gift", event.partialTicks);
                    }
                    // Red Gift
                    else if (itemName.contains("Red Gift")) {
                        highlightBlock(Color.red, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.RED + "Red Gift", event.partialTicks);
                    }
                    // Glacial Talisman
                    else if (itemName.contains("Talisman")) {
                        highlightBlock(Color.orange, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.GOLD + "Talisman", event.partialTicks);
                    }
                    // Glacial Frag
                    else if (itemName.contains("Fragment")) {
                        highlightBlock(Color.magenta, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.LIGHT_PURPLE + "Frag", event.partialTicks);
                    }
                    // Packed Ice
                    else if (itemName.contains("Enchanted Ice")) {
                        highlightBlock(new Color(0x0a0d61), entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_BLUE + "E. Ice", event.partialTicks);
                    }
                    // Enchanted Packed Ice
                    else if (itemName.contains("Enchanted Packed Ice")) {
                        highlightBlock(new Color(0x5317eb), entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_BLUE + "E. Packed Ice", event.partialTicks);
                    }
                    // Enchanted Packed Ice
                    else if (itemName.contains("Glowy Chum Bait")) {
                        highlightBlock(new Color(0x44ad86), entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_AQUA + "Glowy Chum Bait", event.partialTicks);
                    }
                    // Highlight everything else gray
                    else {
                        highlightBlock(Color.lightGray, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, itemName, event.partialTicks);
                    }
                }
            }
        }

        if (SkyblockFeatures.config.icecaveHighlightWalls) GlStateManager.enableDepth();
    }

    public static void highlightBlock(Color c, double d, double d1, double d2, double size, float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d, d1, d2, d + size, d1 + size, d2 + size), c, ticks);
    }
}

