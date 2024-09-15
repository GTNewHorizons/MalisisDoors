package net.malisis.doors.event;

import net.malisis.doors.door.tileentity.MultiTile;
import net.malisis.doors.network.FrameUpdateMessage;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DoorEventHandlerCommon {

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
