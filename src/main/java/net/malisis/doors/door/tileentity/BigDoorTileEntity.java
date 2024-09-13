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

import static net.malisis.doors.door.multiBlock.MultiBlueprint.MB;
import static net.malisis.doors.door.multiBlock.MultiBlueprint.RM;

import java.util.HashMap;
import java.util.Map;

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
import net.malisis.doors.door.multiBlock.MultiBlueprint;
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

import org.joml.Vector3i;

import com.google.common.base.Objects;

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
    private final int[][][] openPrint = { { { 10, -1, -1, 9 }, { MB, RM, RM, 7 }, },
        { { 10, -1, -1, 9 }, { 5, RM, RM, 7 }, }, { { 10, -1, -1, 9 }, { 5, RM, RM, 7 }, },
        { { 10, -1, -1, 9 }, { 5, RM, RM, 7 }, }, { { -1, -1, -1, -1 }, { 0, 0, 0, 0 }, } };

    private final int[][][] closedPrint = { { { RM, -1, -1, RM }, { MB, 0, 0, 0 }, },
        { { RM, -1, -1, RM }, { 0, 0, 0, 0 }, }, { { RM, -1, -1, RM }, { 0, 0, 0, 0 }, },
        { { RM, -1, -1, RM }, { 0, 0, 0, 0 }, }, { { RM, -1, -1, RM }, { 0, 0, 0, 0 }, } };

    // This map defines how to choose the next meta based on a rotation.
    Map<Integer, int[]> metaMap = new HashMap<>() {

        {
            put(0, new int[] { 1, 2, 3 });
            put(1, new int[] { 2, 3, 0 });
            put(2, new int[] { 3, 0, 1 });
            put(3, new int[] { 0, 1, 2 });
            put(4, new int[] { 5, 6, 7 });
            put(5, new int[] { 6, 7, 4 });
            put(6, new int[] { 7, 4, 5 });
            put(7, new int[] { 4, 5, 6 });
            put(8, new int[] { 9, 10, 11 });
            put(9, new int[] { 10, 11, 8 });
            put(10, new int[] { 11, 8, 9 });
            put(11, new int[] { 8, 9, 10 });
        }
    };

    private final MultiBlueprint closedBlueprint = new MultiBlueprint(closedPrint, metaMap, new Vector3i(1, 0, 0));
    private final MultiBlueprint openBlueprint = new MultiBlueprint(openPrint, metaMap, new Vector3i(1, 0, 0));
    private final BigDoor.Type type;
    public BigDoorTileEntity(BigDoor.Type type) {
        this.type = type;
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
    public void setFrameState(Block block) {
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
        this.onStateChange(newState);
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
        if (!this.worldObj.isRemote) {
            this.openOrCloseDoor();
        }
        return true;
    }

    @Override
    public void onCreate(int x, int y, int z, int meta) {
        this.markDirty();
        final int buildHeight = this.worldObj.getHeight() - 6; // No reason to have the door right at world height

        if (y > buildHeight) {
            return;
        }
        this.setMainBlock(x, y, z);
        this.mainBlockSet = true;
        placeBluePrint(this.worldObj, x, y, z, meta, false);

    }

    // This blocks meta is not to be trusted for state. I eventually need to look into why this is but it breaks stuff
    // to try to use the meta. Use this.state instead.
    @Override
    public void onDestroy(TileEntity callingBlock, int meta) {
        if (!this.changingState) {
            int metaToUse = meta;
            if ((this.state == DoorState.OPENING || this.state == DoorState.CLOSING || this.state == DoorState.OPENED ) && metaToUse < 4) {
                metaToUse += 4;
            }
            else if (this.state == DoorState.CLOSED && metaToUse > 3)
            {
                metaToUse -= 4;
            }
            this.changingState = true;
            removeBluePrint(this.worldObj, xCoord, yCoord, zCoord, metaToUse, callingBlock);
            this.changingState = false;
        }
    }

    // This method is a little complex because unless the door is fully closed I want players to be able to go through
    // the door.
    public void onStateChange(DoorState newState) {
        if (this.state == newState) return;
        if (getWorldObj() == null) return; // On startup the worldObj is null for some reason
        int meta = this.getBlockMetadata();
        if (newState == DoorState.OPENING) {
            this.changingState = true;
            if (meta < 4) meta = (meta + 4) % 8;
            placeBluePrint(this.worldObj, xCoord, yCoord, zCoord, meta, true);
            this.changingState = false;
        } else if (newState == DoorState.CLOSED) {
            this.changingState = true;
            if (meta > 3) meta -= 4;
            placeBluePrint(this.worldObj, xCoord, yCoord, zCoord, meta, true);
            this.changingState = false;
        }
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound packetData = pkt.func_148857_g();
        this.setFrameState(BlockState.fromNBT(packetData));
    }

    @Override
    public void placeBluePrint(World world, int x, int y, int z, int meta, boolean removeBlockInWay) {
        MultiBlueprint print = (meta < 4 ? this.closedBlueprint : this.openBlueprint);
        switch (meta) {
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

    private void bluePrintPlacerHelper(World world, int x, int y, int z, MultiBlueprint print,
        boolean removeBlockInWay) {
        int mainBlockRelativeX = print.startingLocation.x;
        int mainBlockRelativeY = print.startingLocation.y;
        int mainBlockRelativeZ = print.startingLocation.z;
        for (int j = 0; j < print.bluePrint.length; j++) // y
        {
            for (int i = 0; i < print.bluePrint[0].length; i++) // x
            {
                for (int k = 0; k < print.bluePrint[0][0].length; k++) // z
                {
                    if (!(i == print.startingLocation.x && j == print.startingLocation.y
                        && k == print.startingLocation.z) && print.bluePrint[j][i][k] > -1) {

                        switch(this.type)
                        {
                            case CARRIAGE ->
                                ((CollisionHelperBlock) MalisisDoors.Blocks.collisionHelperBlockCarriage).makeCollisionHelperBlock(
                                    world,
                                    x - mainBlockRelativeX + i,
                                    y - mainBlockRelativeY + j,
                                    z + mainBlockRelativeZ - k,
                                    print.bluePrint[j][i][k],
                                    this.xCoord,
                                    this.yCoord,
                                    this.zCoord,
                                    this.getBlockMetadata());
                            case MEDIEVAL ->
                                ((CollisionHelperBlock) MalisisDoors.Blocks.collisionHelperBlockMedieval).makeCollisionHelperBlock(
                                    world,
                                    x - mainBlockRelativeX + i,
                                    y - mainBlockRelativeY + j,
                                    z + mainBlockRelativeZ - k,
                                    print.bluePrint[j][i][k],
                                    this.xCoord,
                                    this.yCoord,
                                    this.zCoord,
                                    this.getBlockMetadata());
                        }
                    } else if (print.bluePrint[j][i][k] == Integer.MIN_VALUE && removeBlockInWay) {
                        world.setBlockToAir(
                            x - mainBlockRelativeX + i,
                            y - mainBlockRelativeY + j,
                            z + mainBlockRelativeZ - k);
                    }
                }
            }
        }
    }

    @Override
    public void removeBluePrint(World world, int x, int y, int z, int meta, TileEntity callingBlock) {
        MultiBlueprint print = (meta < 4 ? this.closedBlueprint : this.openBlueprint);
        switch (meta) {
            case 0, 4:
                this.bluePrintRemovalHelper(world, x, y, z, print, callingBlock);
                break;
            case 1, 5:
                print.rotate(MultiBlueprint.RotationDegrees.ROT90);
                this.bluePrintRemovalHelper(world, x, y, z, print, callingBlock);
                print.rotate(MultiBlueprint.RotationDegrees.ROT270);
                break;
            case 2, 6:
                print.rotate(MultiBlueprint.RotationDegrees.ROT180);
                this.bluePrintRemovalHelper(world, x, y, z, print, callingBlock);
                print.rotate(MultiBlueprint.RotationDegrees.ROT180);
                break;
            case 3, 7:
                print.rotate(MultiBlueprint.RotationDegrees.ROT270);
                this.bluePrintRemovalHelper(world, x, y, z, print, callingBlock);
                print.rotate(MultiBlueprint.RotationDegrees.ROT90);
                break;
        }
    }

    private void bluePrintRemovalHelper(World world, int x, int y, int z, MultiBlueprint print, TileEntity callingBlock) {

        Block blockToDrop = this.type == BigDoor.Type.CARRIAGE ? MalisisDoors.Blocks.carriageDoor : MalisisDoors.Blocks.medievalDoor;
        int mainBlockRelativeX = print.startingLocation.x;
        int mainBlockRelativeY = print.startingLocation.y;
        int mainBlockRelativeZ = print.startingLocation.z;
        for (int j = 0; j < print.bluePrint.length; j++) // y
        {
            for (int i = 0; i < print.bluePrint[0].length; i++) // x
            {
                for (int k = 0; k < print.bluePrint[0][0].length; k++) // z
                {
                    if (print.bluePrint[j][i][k] == MB) {
                        ((MultiTile) callingBlock).dropMainBlockAtLocation(blockToDrop);
                    }
                    if (print.bluePrint[j][i][k] > -1) {
                        world.setBlockToAir(
                            x - mainBlockRelativeX + i,
                            y - mainBlockRelativeY + j,
                            z + mainBlockRelativeZ - k);
                    }
                }
            }
        }
    }
}
