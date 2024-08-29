package net.malisis.doors.door.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CollisionHelperTileEntity extends MultiCollisionTile
{
    public void setMainBlock(int x, int y, int z)
    {
        this.mainBlockX = x;
        this.mainBlockY = y;
        this.mainBlockZ = z;
        this.markDirty();
        if (!this.worldObj.isRemote)
        {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public void onBlockRemoval()
    {
        TileEntity mainBlock = getMainBlockTile();
        if (mainBlock != null)
        {
            if (mainBlock instanceof IMultiDoor)
            {
                ((IMultiDoor) mainBlock).onDestroy(this);
            }
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer) {
        TileEntity mainBlock = getMainBlockTile();
        if (mainBlock != null) {
            final TileEntity tileEntity = this.worldObj
                .getTileEntity(this.mainBlockX, this.mainBlockY, this.mainBlockZ);
            if (tileEntity instanceof IMultiDoor) {
                return ((IMultiDoor) tileEntity).onActivated(par5EntityPlayer);
            }
        }
        return false;
    }

    public TileEntity getMainBlockTile() {

        return this.worldObj.getTileEntity(this.mainBlockX, this.mainBlockY, this.mainBlockZ);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("mainBlockX", this.mainBlockX);
        nbt.setInteger("mainBlockY", this.mainBlockY);
        nbt.setInteger("mainBlockZ", this.mainBlockZ);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.mainBlockX = nbt.getInteger("mainBlockX");
        this.mainBlockY = nbt.getInteger("mainBlockY");
        this.mainBlockZ = nbt.getInteger("mainBlockZ");
    }
}
