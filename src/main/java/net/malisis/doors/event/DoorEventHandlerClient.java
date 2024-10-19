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

public class DoorEventHandlerClient {

    public CustomDoorBoundingBoxRenderer cdbbRenderer = new CustomDoorBoundingBoxRenderer();

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        World world = event.player.worldObj;
        MovingObjectPosition target = event.target;

        if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int x = target.blockX;
            int y = target.blockY;
            int z = target.blockZ;
            Block block = world.getBlock(x, y, z);

            if (!(block instanceof CollisionHelperBlock collisionHelper)) return;
            if (world.getTileEntity(x, y, z) instanceof MultiTile multi
                && multi.getMainBlockTile() instanceof DoorTileEntity doorTile) {
                DoorState state = doorTile.getState();
                if (state == DoorState.OPENED || state == DoorState.OPENING || state == DoorState.CLOSING) {
                    event.setCanceled(true);
                    cdbbRenderer
                        .renderOpenDoorBoundingBox(world, collisionHelper, event.player, event.partialTicks, target);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientInteract(PlayerInteractEvent event) {
        if (!event.world.isRemote) return;
        EntityPlayer player = event.entityPlayer;
        if (player.isSneaking() && event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            TileEntity tileEntity = getTileEntityLookingAt(player);
            if (tileEntity instanceof MultiTile multiTile && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                ItemStack heldStack = player.getHeldItem();
                if (heldStack == null) return;

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
            return mc.theWorld.getTileEntity(rayTraceResult.blockX, rayTraceResult.blockY, rayTraceResult.blockZ);
        }
        return null;
    }
}
