/*
 * The MIT License (MIT) Copyright (c) 2014 Ordinastie Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.malisis.doors.door.tileentity;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.CollisionHelperBlock;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.CarriageDoorMovement;
import net.malisis.doors.door.sound.CarriageDoorSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Objects;

import static net.minecraft.util.MathHelper.abs;

/**
 * @author Ordinastie
 *
 */
public class BigDoorTileEntity extends DoorTileEntity implements IMultiDoor {

    private boolean delete = false;
    private boolean processed = true;
    private ForgeDirection direction = ForgeDirection.NORTH;

    private BlockState frameState;

    public BigDoorTileEntity() {
        DoorDescriptor descriptor = new DoorDescriptor();
        descriptor.setMovement(DoorRegistry.getMovement(CarriageDoorMovement.class));
        descriptor.setSound(DoorRegistry.getSound(CarriageDoorSound.class));
        descriptor.setDoubleDoor(false);
        descriptor.setOpeningTime(20);
        setDescriptor(descriptor);

        frameState = new BlockState(Blocks.quartz_block);
    }

    public BlockState getFrameState() {
        return frameState;
    }

    public void setFrameState(BlockState state) {
        if (state != null) frameState = state;
    }

    @Override
    public boolean isTopBlock(int x, int y, int z) {
        return false;
    }

    @Override
    public boolean isReversed() {
        return false;
    }

    @Override
    public boolean isPowered() {
        return false;
    }

    @Override
    public void setDoorState(DoorState newState) {
        boolean moving = this.moving;
        BlockState state = null;
        if (getWorldObj() != null) {
            state = new BlockState(xCoord, yCoord, zCoord, getBlockType());
        }

        super.setDoorState(newState);
    }

    @Override
    public void updateEntity() {
        if (!processed && getWorldObj() != null) {
            if (delete) {
                MalisisCore.log.info("Deleting " + xCoord + "," + yCoord + "," + zCoord);
                getWorldObj().setBlockToAir(xCoord, yCoord, zCoord);
            } else {
                MalisisCore.log.info("Adding to chunk : " + xCoord + "," + yCoord + "," + zCoord);
                getWorldObj().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, Door.dirToInt(direction), 2);
                processed = true;
            }
            return;
        }
        super.updateEntity();
    }

    public ItemStack getDroppedItemStack() {
        ItemStack itemStack = new ItemStack(getBlockType());
        NBTTagCompound nbt = new NBTTagCompound();
        BlockState.toNBT(nbt, frameState);
        itemStack.setTagCompound(nbt);
        return itemStack;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("multiBlock")) {
            MultiBlock mb = new MultiBlock(tag);
            delete = !mb.isOrigin(xCoord, yCoord, zCoord);
            direction = mb.getDirection();
            processed = false;
        }

        frameState = Objects.firstNonNull(BlockState.fromNBT(tag), new BlockState(Blocks.quartz_block));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        BlockState.toNBT(nbt, frameState);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return ((BigDoor) getBlockType())
            .getBoundingBox(getWorldObj(), xCoord, yCoord, zCoord, BoundingBoxType.RENDER)[0]
                .offset(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean onActivated(EntityPlayer entityPlayer) {
        return false;
    }

    @Override
    public void onCreate(int x, int y, int z, int meta) {
        this.markDirty();
        final int buildHeight = this.worldObj.getHeight() - 6; // No reason to have the door right at world height

        if (y > buildHeight)
        {
            return;
        }

        // 4 states based on the meta of the main
        // meta 0: going South to North on the East side of the block  - facing west
        // meta 1: going West to East on the South side of the block - facing north
        // meta 2: going North to South on the West side of the block - facing east
        // meta 3: going East to West on the North side of the block - facing south

        boolean widthDirectionFlag = meta % 2 == 0;

        int xStep = meta == 3 ? -1 : 1;
        int zStep = meta == 0 ? -1 : 1;

        int xMax = widthDirectionFlag ? 1 : 4;
        int zMax = widthDirectionFlag ? 4 : 1;

        for (int yLoc = 0; yLoc < 5; yLoc++)
        {
            for (int xLoc = 0; abs(xLoc) < xMax; xLoc += xStep)
            {
                for (int zLoc = 0; abs(zLoc) < zMax; zLoc += zStep)
                {
                    if (!(yLoc == 0 && zLoc == 0 && xLoc == 0))
                    {
                        ((CollisionHelperBlock) MalisisDoors.Blocks.collisionHelperBlock).makeCollisionHelperBlock(this.worldObj, x + xLoc, y + yLoc, z + zLoc, x, y, z, meta);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy(TileEntity callingBlock)
    {

    }
}
