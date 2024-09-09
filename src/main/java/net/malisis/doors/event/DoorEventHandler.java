package net.malisis.doors.event;

import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.CollisionHelperBlock;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.MultiTile;
import net.malisis.doors.network.FrameUpdateMessage;
import net.malisis.doors.renderer.CustomDoorBoundingBoxRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DoorEventHandler {

    public CustomDoorBoundingBoxRenderer cdbbRenderer = new CustomDoorBoundingBoxRenderer();

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        World world = event.player.worldObj;
        if (world.isRemote) {
            MovingObjectPosition target = event.target;

            if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int x = target.blockX;
                int y = target.blockY;
                int z = target.blockZ;
                Block block = world.getBlock(x, y, z);

                if (block instanceof CollisionHelperBlock) {
                    MultiTile cTE = ((MultiTile) world.getTileEntity(x, y, z));
                    TileEntity mainDoorTE = cTE.getMainBlockTile();
                    if (mainDoorTE instanceof DoorTileEntity) {
                        DoorState state = ((DoorTileEntity) mainDoorTE).getState();
                        if (state == DoorState.OPENED || state == DoorState.OPENING || state == DoorState.CLOSING) {
                            event.setCanceled(true);
                            cdbbRenderer.renderOpenDoorBoundingBox(
                                world,
                                (CollisionHelperBlock) block,
                                event.player,
                                event.partialTicks,
                                target);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        TileEntity tileEntity = getTileEntityLookingAt(event.entityPlayer);
        if (event.entityPlayer.isSneaking() && event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            && tileEntity instanceof MultiTile multiTile
            && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            ItemStack heldStack = event.entityPlayer.getHeldItem();
            if (heldStack != null) {
                Block block = Block.getBlockFromItem(heldStack.getItem());
                int damageValue = heldStack.getItemDamage();
                if (block != null) {
                    event.setCanceled(true);
                    FrameUpdateMessage.SendFrameUpdateMessage(multiTile, block, damageValue);
                }
            }
        }
    }

    private TileEntity getTileEntityLookingAt(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        double maxReach = mc.playerController.getBlockReachDistance();
        Vec3 eyePosition = player.getPosition(1.0F);
        Vec3 lookVector = player.getLook(1.0F);
        Vec3 endPosition = eyePosition
            .addVector(lookVector.xCoord * maxReach, lookVector.yCoord * maxReach, lookVector.zCoord * maxReach);
        MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(eyePosition, endPosition);
        if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int blockX = rayTraceResult.blockX;
            int blockY = rayTraceResult.blockY;
            int blockZ = rayTraceResult.blockZ;
            return mc.theWorld.getTileEntity(blockX, blockY, blockZ);
        }
        return null;
    }
}
