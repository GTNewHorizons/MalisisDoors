package net.malisis.doors.door.item;

import net.malisis.core.util.EntityUtils;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.Door;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BigDoorItem extends ItemBlock {

    public BigDoorItem(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        if (player == null) return false;
        int direction = Door.dirToInt(EntityUtils.getEntityFacing(player));
        BigDoor door = (BigDoor) field_150939_a;
        if (!door.checkAreaClearForDoor(world, x, y, z, direction)) {
            if (!world.isRemote) player.addChatMessage(new ChatComponentText("There's no room for the door!"));
            return false;
        }
        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, direction);
    }
}
