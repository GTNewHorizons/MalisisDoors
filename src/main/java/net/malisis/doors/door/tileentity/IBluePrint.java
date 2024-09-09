package net.malisis.doors.door.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IBluePrint
{
    public void placeBluePrint(World world, int x, int y, int z, int meta, boolean removeBlockInWay);
    public void removeBluePrint(World world, int x, int y, int z, int meta, TileEntity callingBlock, Block blockToDrop);
}
