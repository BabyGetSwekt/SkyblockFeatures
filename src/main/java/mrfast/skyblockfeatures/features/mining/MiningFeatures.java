package mrfast.skyblockfeatures.features.mining;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.Sys;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.BlockChangeEvent;
import mrfast.skyblockfeatures.events.PacketEvent;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.ScoreboardUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiningFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock || event.type == 2) return;

        String unformatted = Utils.cleanColor(event.message.getUnformattedText());
        if(SkyblockFeatures.config.treasureChestSolver && unformatted.contains("uncovered a treasure chest!")) {
            treasureChest = null;
            particles.clear();
            progress = 0;
        }
    }
    @SubscribeEvent
    public void onPlayerInteractEvent(BlockChangeEvent event) {
        if(Utils.inSkyblock && SkyblockFeatures.config.highlightEnderNodes) {
            BlockPos p1 = Utils.GetMC().thePlayer.getPosition();
            BlockPos p2 = event.pos;
            if(p1.distanceSq(p2.getX(), p2.getY(), p2.getZ())<100) enderParticles.clear();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock) return;
        if(SkyblockFeatures.config.highlightEnderNodes && SkyblockInfo.getInstance().getMap().contains("The End")) {
            try {
                if(SkyblockFeatures.config.highlightEnderNodesWalls) GlStateManager.disableDepth();
                List<Vec3> drawnPositions = new ArrayList<Vec3>();

                for(Vec3 packet:enderParticles) {
                    boolean dupe =false;
                    double x = packet.xCoord;
                    double y = packet.yCoord;
                    double z = packet.zCoord;
                    if(!drawnPositions.contains(packet)) {
                        for(Vec3 packet2:drawnPositions) {
                            if(packet.distanceTo(packet2)<1.5) {
                                dupe=true;
                            }
                        }
                        if(dupe) continue;
                    }
                    if ((x - Math.floor(x)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 1.25), Math.round(y - 0.5), Math.round(z - 0.5));
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        Color color = block instanceof BlockObsidian ? new Color(0x4f024f) : Color.magenta;
                        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, event.partialTicks);
                        drawnPositions.add(packet);
                    }
                    if ((y - Math.floor(y)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x-1), Math.round(y - 1.25), Math.round(z - 0.5));
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        Color color = block instanceof BlockObsidian ? new Color(0x4f024f) : Color.magenta;
                        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, event.partialTicks);
                        drawnPositions.add(packet);
                    }
                    if ((z - Math.floor(z)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 0.5), Math.round(y - 0.5), Math.round(z - 1.25));
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        Color color = block instanceof BlockObsidian ? new Color(0x4f024f) : Color.magenta;
                        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, event.partialTicks);
                        drawnPositions.add(packet);
                    }
                    if ((x - Math.floor(x)) == 0.75) {
                        BlockPos blockPos = new BlockPos(Math.round(x + 0.25), Math.round(y - 0.5), Math.round(z - 0.5));
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        Color color = block instanceof BlockObsidian ? new Color(0x4f024f) : Color.magenta;
                        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, event.partialTicks);
                        drawnPositions.add(packet);
                    }
                    if ((z - Math.floor(z)) == 0.75) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 0.5), Math.round(y - 0.5), Math.round(z + 0.25));
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        Color color = block instanceof BlockObsidian ? new Color(0x4f024f) : Color.magenta;
                        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, event.partialTicks);
                        drawnPositions.add(packet);
                    }
                }
                GlStateManager.enableDepth();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        
        try {
            if(!SkyblockFeatures.config.treasureChestSolver || !SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) return;
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(treasureChest)).getBlock();
            if(treasureChest != null) {
                Vec3 stringPos = new Vec3(treasureChest.getX()+0.5, treasureChest.getY()+1.1, treasureChest.getZ()+0.5);
                GlStateManager.disableDepth();
                RenderUtil.draw3DString(stringPos, ChatFormatting.AQUA+""+progress+" / 5", 0xFFFFFF, event.partialTicks);
                GlStateManager.enableDepth();
                // RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(treasureChest, treasureChest.add(1, 1, 1)), Color.green, event.partialTicks);
            }
            for(Vec3 packet:particles) {
                RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(packet.xCoord-0.05, packet.yCoord-0.05, packet.zCoord-0.05, packet.xCoord+0.1, packet.yCoord+0.1, packet.zCoord+0.1), Color.red, event.partialTicks);
                if(block != null && block == Blocks.air) {
                    particles.remove(packet);
                }
            }
            if(block != null && block == Blocks.air) {
                treasureChest = null;
                progress = 0;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        try {
            treasureChest = null;
            particles.clear();
            enderParticles.clear();
            progress = 0;
        } catch(Exception e) {

        }
    }
    BlockPos treasureChest = null;
    List<Vec3> particles = new ArrayList<Vec3>();
    List<Vec3> enderParticles = new ArrayList<Vec3>();
    int progress = 0;

    @SubscribeEvent
    public void onRecievePacket(PacketEvent.ReceiveEvent event) {
        if(event.packet instanceof S2APacketParticles && SkyblockFeatures.config.highlightEnderNodes) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            Vec3 pos = new Vec3(packet.getXCoordinate(),packet.getYCoordinate(),packet.getZCoordinate());
            if(type == EnumParticleTypes.PORTAL && !enderParticles.contains(pos)) {
                enderParticles.add(pos);
            }
        }
        if(event.packet instanceof S2APacketParticles && SkyblockFeatures.config.treasureChestSolver) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            Vec3 pos = new Vec3(packet.getXCoordinate(),packet.getYCoordinate(),packet.getZCoordinate());
            boolean dupe = false;
            for(Vec3 part:particles) {
                if(pos.distanceTo(part)<0.05) {
                    dupe = true;
                }
                if(part.distanceTo(pos) > 0.05) {
                    particles.clear();
                    break;
                }
            }
            
            if(!dupe && type == EnumParticleTypes.CRIT && !particles.contains(pos) && mc.thePlayer.getDistance(pos.xCoord, pos.yCoord, pos.zCoord)<5) {
                if(treasureChest == null) {
                    particles.add(pos);
                    for(TileEntity entity: mc.theWorld.loadedTileEntityList) {
                        if(entity.getPos().distanceSq(pos.xCoord, pos.yCoord, pos.zCoord) < 2 && entity instanceof TileEntityChest) {
                            treasureChest = entity.getPos();
                        }
                    }
                } else {
                    if(treasureChest.distanceSq(pos.xCoord, pos.yCoord, pos.zCoord) < 2) particles.add(pos);
                }
            }
        }
        if(event.packet instanceof S29PacketSoundEffect && SkyblockFeatures.config.treasureChestSolver) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if(packet.getSoundName().contains("orb")) {
                if(packet.getVolume() == 1 && packet.getPitch() == 1) {
                    progress++;
                }
            }
            if(packet.getSoundName().contains("villager") && packet.getSoundName().contains("no")) {
                progress=0;
            }
        }
    }
}
