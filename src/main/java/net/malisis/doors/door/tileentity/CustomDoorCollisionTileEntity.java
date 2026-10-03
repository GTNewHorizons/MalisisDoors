package net.malisis.doors.door.tileentity;

import net.malisis.doors.door.block.CustomDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class CustomDoorCollisionTileEntity extends TileEntity {

    private boolean linked;
    private int ownerX;
    private int ownerY;
    private int ownerZ;

    public void setOwner(CustomDoorTileEntity owner) {
        linked = true;
        ownerX = owner.xCoord;
        ownerY = owner.yCoord;
        ownerZ = owner.zCoord;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean belongsTo(CustomDoorTileEntity owner) {
        return linked && ownerX == owner.xCoord && ownerY == owner.yCoord && ownerZ == owner.zCoord;
    }

    public CustomDoorTileEntity getOwner(IBlockAccess world) {
        if (!linked || worldObj != null && !worldObj.blockExists(ownerX, ownerY, ownerZ)
            || !(world.getBlock(ownerX, ownerY, ownerZ) instanceof CustomDoor)) return null;
        TileEntity owner = world.getTileEntity(ownerX, ownerY, ownerZ);
        return owner instanceof CustomDoorTileEntity ? (CustomDoorTileEntity) owner : null;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        if (linked && !worldObj.blockExists(ownerX, ownerY, ownerZ)) return;
        CustomDoorTileEntity owner = getOwner(worldObj);
        if (owner == null || !owner.needsCollisionHelpers() || !owner.isHelperCell(xCoord, yCoord, zCoord))
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        linked = nbt.getBoolean("linked");
        ownerX = nbt.getInteger("ownerX");
        ownerY = nbt.getInteger("ownerY");
        ownerZ = nbt.getInteger("ownerZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("linked", linked);
        nbt.setInteger("ownerX", ownerX);
        nbt.setInteger("ownerY", ownerY);
        nbt.setInteger("ownerZ", ownerZ);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
