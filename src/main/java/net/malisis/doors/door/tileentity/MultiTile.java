package net.malisis.doors.door.tileentity;


import net.malisis.core.util.BlockState;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MultiTile extends DoorTileEntity {
    public int mainBlockX;
    public int mainBlockY;
    public int mainBlockZ;
    public int mainBlockMeta;
    public boolean mainBlockSet;

    public void setMainBlock(int x, int y, int z)
    {
        this.mainBlockX = x;
        this.mainBlockY = y;
        this.mainBlockZ = z;
        this.mainBlockSet = true;
        this.markDirty();
        if (!this.worldObj.isRemote)
        {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public void onBlockRemoval(Block block)
    {
        TileEntity mainBlock = getMainBlockTile();
        if (mainBlock != null)
        {
            if (mainBlock instanceof IMultiBlock)
            {
                ((IMultiBlock) mainBlock).onDestroy(block, this, this.mainBlockMeta);
            }
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player) {
        world.markBlockForUpdate(x, y, z);
        TileEntity mainBlock = getMainBlockTile();
        if (mainBlock instanceof IMultiBlock) {
            return ((IMultiBlock) mainBlock).onActivated(player);
        }
        return true;
    }

    public TileEntity getMainBlockTile() {

        return this.worldObj.getTileEntity(this.mainBlockX, this.mainBlockY, this.mainBlockZ);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        super.onDataPacket(net, pkt);
        NBTTagCompound packetData = pkt.func_148857_g();
        this.mainBlockX = packetData.getInteger("mainBlockX");
        this.mainBlockY = packetData.getInteger("mainBlockY");
        this.mainBlockZ = packetData.getInteger("mainBlockZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("mainBlockX", this.mainBlockX);
        nbt.setInteger("mainBlockY", this.mainBlockY);
        nbt.setInteger("mainBlockZ", this.mainBlockZ);
        nbt.setInteger("mainBlockMeta", this.mainBlockMeta);
        nbt.setBoolean("mainBlockSet", this.mainBlockSet);

    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.mainBlockX = nbt.getInteger("mainBlockX");
        this.mainBlockY = nbt.getInteger("mainBlockY");
        this.mainBlockZ = nbt.getInteger("mainBlockZ");
        this.mainBlockMeta = nbt.getInteger("mainBlockMeta");
        this.mainBlockSet = nbt.getBoolean("mainBlockSet");
    }

    @Override
    public boolean shouldRender()
    {
        return false;
    }

    public void setFrameState(Block block)
    {
        TileEntity mainTile = this.getMainBlockTile();
        if (mainTile instanceof BigDoorTileEntity bigDoorMainTile)
        {
            bigDoorMainTile.setFrameState(block);
        }
    }

    public void setFrameState(BlockState blockState)
    {
        TileEntity mainTile = this.getMainBlockTile();
        if (mainTile instanceof BigDoorTileEntity bigDoorTileEntity)
        {
            bigDoorTileEntity.setFrameState(blockState);
        }
    }

    public void dropMainBlockAtLocation(Block block)
    {
        if (mainBlockSet)
        {
            int meta = this.getBlockMetadata();
            block.dropBlockAsItem(this.worldObj, xCoord, yCoord, zCoord, meta, 0);
        }
    }

    public void setMainBlockMeta(int meta)
    {
        this.mainBlockMeta = meta;
    }
}
