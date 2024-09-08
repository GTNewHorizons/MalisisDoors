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
import net.malisis.doors.door.multiBlock.MultiBlueprint;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.CollisionHelperBlock;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.CarriageDoorMovement;
import net.malisis.doors.door.sound.CarriageDoorSound;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Objects;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

import static net.malisis.doors.door.multiBlock.MultiBlueprint.MB;
import static net.malisis.doors.door.multiBlock.MultiBlueprint.RM;
import static net.minecraft.util.MathHelper.abs;

/**
 * @author Ordinastie
 *
 */
public class BigDoorTileEntity extends MultiTile implements IMultiBlock, IBluePrint {

    private boolean delete = false;
    private boolean processed = true;
    private ForgeDirection direction = ForgeDirection.NORTH;
    private final Block defaultBorderBlock = Blocks.stonebrick;
    private BlockState frameState = new BlockState(defaultBorderBlock);
    private Boolean changingState = false;
    private final int[][][] openPrint =
    {
        {
            {10, -1, -1,  9},
            {MB, RM, RM,  7},
        },
        {
            {10, -1, -1,  9},
            { 5, RM, RM,  7},
        },
        {
            {10, -1, -1,  9},
            { 5, RM, RM,  7},
        },
        {
            {10, -1, -1,  9},
            { 5, RM, RM,  7},
        },
        {
            {-1, -1, -1, -1},
            { 0,  0,  0,  0},
        }
    };

    private final int[][][] closedPrint =
    {
        {
            {RM, -1, -1,  RM},
            {MB,  0,  0,  0},
        },
        {
            {RM, -1, -1,  RM},
            { 0,  0,  0,  0},
        },
        {
            {RM, -1, -1,  RM},
            { 0,  0,  0,  0},
        },
        {
            {RM, -1, -1,  RM},
            { 0,  0,  0,  0},
        },
        {
            {RM, -1, -1,  RM},
            { 0,  0,  0,  0},
        }
    };



    // This map defines how to choose the next meta based on a rotation.
    Map<Integer, int[]> metaMap = new HashMap<>() {{
        put(0, new int[]{1, 2, 3});
        put(1, new int[]{2, 3, 0});
        put(2, new int[]{3, 0, 1});
        put(3, new int[]{0, 1, 2});
        put(4, new int[]{5, 6, 7});
        put(5, new int[]{6, 7, 4});
        put(6, new int[]{7, 4, 5});
        put(7, new int[]{4, 5, 6});
        put(8, new int[]{9, 10, 11});
        put(9, new int[]{10, 11, 8});
        put(10, new int[]{ 11, 8, 9});
        put(11, new int[]{8, 9, 10});
    }};

    private final MultiBlueprint closedBlueprint = new MultiBlueprint(closedPrint, metaMap, new Vector3i(1,0,0));
    private final MultiBlueprint openBlueprint = new MultiBlueprint(openPrint, metaMap, new Vector3i(1, 0, 0));

    public BigDoorTileEntity() {
        DoorDescriptor descriptor = new DoorDescriptor();
        descriptor.setMovement(DoorRegistry.getMovement(CarriageDoorMovement.class));
        descriptor.setSound(DoorRegistry.getSound(CarriageDoorSound.class));
        descriptor.setDoubleDoor(false);
        descriptor.setOpeningTime(20);
        setDescriptor(descriptor);
    }

    public BlockState getFrameState() {
        return frameState;
    }

    public void setFrameState(BlockState state) {
        if (state != null) frameState = state;
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void setFrameState (Block block)
    {
        this.setFrameState(new BlockState(block));
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

        frameState = Objects.firstNonNull(BlockState.fromNBT(tag), new BlockState(defaultBorderBlock));
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
        if (!this.worldObj.isRemote)
        {
            this.openOrCloseDoor();
        }
        this.onStateChange();
        return true;
    }

    @Override
    public void onCreate(int x, int y, int z, int meta) {
        this.markDirty();
        final int buildHeight = this.worldObj.getHeight() - 6; // No reason to have the door right at world height

        if (y > buildHeight) {
            return;
        }
        placeBluePrint(this.worldObj, x, y, z, meta, false);
    }

    @Override
    public void onDestroy(TileEntity callingBlock)
    {
        int meta = this.getBlockMetadata();
        boolean widthDirectionFlag = meta % 2 == 0;

        if (meta < 4) {

            int xStep = meta == 3 ? -1 : 1;
            int zStep = meta == 0 ? -1 : 1;

            int xMax = widthDirectionFlag ? 1 : 4;
            int zMax = widthDirectionFlag ? 4 : 1;

            for (int yLoc = 0; yLoc < 5; yLoc++) {
                for (int xLoc = 0; abs(xLoc) < xMax; xLoc += xStep) {
                    for (int zLoc = 0; abs(zLoc) < zMax; zLoc += zStep) {
                        if (xLoc == 0 && yLoc == 0 && zLoc == 0) {
                            ((MultiTile) callingBlock).dropMainBlockAtLocation();
                        }
                        this.worldObj.setBlockToAir(xLoc + this.xCoord, yLoc + this.yCoord, zLoc + this.zCoord);
                    }
                }
            }
        }
        else
        {
            // Need to remove blocks if we're in the open state.
        }
    }

    @Override
    public boolean isChangingState() {
        return this.changingState;
    }

    public void onStateChange()
    {
        this.changingState = true;
        int meta = this.getBlockMetadata();
        placeBluePrint(this.worldObj, xCoord, yCoord, zCoord, (meta + 4) % 8, true);
        changingState = false;
    }
    @Override
    public boolean shouldRender()
    {
        return true;
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
        this.setFrameState(BlockState.fromNBT(packetData));
    }

    @Override
    public void placeBluePrint(World world, int x, int y, int z, int meta, boolean removeBlockInWay)
    {
        MultiBlueprint print = (meta < 4 ? this.closedBlueprint : this.openBlueprint);
        switch (meta)
        {
            case 0, 4:
                this.bluePrintPlacerHelper(world, x, y, z, print, removeBlockInWay);
                break;
            case 1, 5:
                print.rotate(MultiBlueprint.RotationDegrees.ROT90);
                this.bluePrintPlacerHelper(world, x, y, z, print, removeBlockInWay);
                print.rotate(MultiBlueprint.RotationDegrees.ROT270);
                break;
            case 2, 6:
                print.rotate(MultiBlueprint.RotationDegrees.ROT180);
                this.bluePrintPlacerHelper(world, x, y, z, print, removeBlockInWay);
                print.rotate(MultiBlueprint.RotationDegrees.ROT180);
                break;
            case 3, 7:
                print.rotate(MultiBlueprint.RotationDegrees.ROT270);
                this.bluePrintPlacerHelper(world, x, y, z, print, removeBlockInWay);
                print.rotate(MultiBlueprint.RotationDegrees.ROT90);
                break;
        }
    }

    private void bluePrintPlacerHelper(World world, int x, int y, int z, MultiBlueprint print, boolean removeBlockInWay)
    {
        int mainBlockRelativeX = print.startingLocation.x;
        int mainBlockRelativeY = print.startingLocation.y;
        int mainBlockRelativeZ = print.startingLocation.z;
        for (int j = 0; j < print.bluePrint.length; j++) // y
        {
            for (int i = 0; i < print.bluePrint[0].length; i++) // x
            {
                for (int k = 0; k < print.bluePrint[0][0].length; k++) // z
                {
                    if (!(i == print.startingLocation.x && j == print.startingLocation.y && k == print.startingLocation.z) && print.bluePrint[j][i][k] > -1)
                    {
                        ((CollisionHelperBlock) MalisisDoors.Blocks.collisionHelperBlock).makeCollisionHelperBlock(world, x - mainBlockRelativeX + i, y - mainBlockRelativeY + j, z + mainBlockRelativeZ - k, this.xCoord, this.yCoord, this.zCoord, print.bluePrint[j][i][k]);
                    }
                    else if(print.bluePrint[j][i][k] == Integer.MIN_VALUE && removeBlockInWay)
                    {
                        world.setBlockToAir(x - mainBlockRelativeX + i, y - mainBlockRelativeY + j, z + mainBlockRelativeZ - k);
                    }
                }
            }
        }
    }

    @Override
    public void removeBluePrint(World world, int x, int y, int z, int meta) {
        return;
    }
}
