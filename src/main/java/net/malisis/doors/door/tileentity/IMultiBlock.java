package net.malisis.doors.door.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface IMultiBlock {

    /**
     * Called when activated
     */
    boolean onActivated(EntityPlayer entityPlayer);

    /**
     * Called when this multiblock is created
     *
     * @param x - placed x coord
     * @param y - placed y coord
     * @param z - placed z coord'
     * @param meta - meta of the placed door
     */
    void onCreate(int x, int y, int z, int meta);

    /**
     * Called when one of the multiblocks of this block is destroyed
     *
     * @param callingBlock - The tile entity who called the onDestroy function
     */
    void onDestroy(TileEntity callingBlock);

}
