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

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.util.AABBUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.CustomDoor;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.DoubleRotateMovement;
import net.malisis.doors.door.movement.RotateAndPlaceMovement;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

/**
 * @author Ordinastie
 *
 */
public class CustomDoorTileEntity extends DoorTileEntity {

    public int getMaterialRenderPass(Block material) {
        return material != null && material.getRenderBlockPass() == 1 ? 1 : 0;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return frame != null && getMaterialRenderPass(frame) == pass
            || topMaterial != null && getMaterialRenderPass(topMaterial) == pass
            || bottomMaterial != null && getMaterialRenderPass(bottomMaterial) == pass;
    }

    private boolean removing;

    public boolean isRemoving() {
        return removing;
    }

    public void beginRemoval() {
        removing = true;
    }

    private AxisAlignedBB getOpenWorldBounds() {
        AxisAlignedBB box;
        if (getMovement() instanceof RotateAndPlaceMovement)
            box = ((RotateAndPlaceMovement) getMovement()).getOpenBoundingBox(this);
        else if (getMovement() instanceof DoubleRotateMovement)
            box = ((DoubleRotateMovement) getMovement()).getOpenBoundingBox(this);
        else return null;
        box.maxY++;
        if (isCentered()) box.offset(0, 0, 0.5F - Door.DOOR_WIDTH / 2);
        AABBUtils.rotate(box, Door.intToDir(getDirection()));
        return box.offset(xCoord, yCoord, zCoord);
    }

    public boolean needsCollisionHelpers() {
        return !removing
            && (getMovement() instanceof RotateAndPlaceMovement || getMovement() instanceof DoubleRotateMovement)
            && (getState() == DoorState.OPENING || getState() == DoorState.OPENED);
    }

    public boolean isHelperCell(int x, int y, int z) {
        AxisAlignedBB box = getOpenWorldBounds();
        return box != null && (x != xCoord || z != zCoord)
            && box.intersectsWith(AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
    }

    private List<ChunkCoordinates> getHelperCells() {
        List<ChunkCoordinates> cells = new ArrayList<>();
        AxisAlignedBB box = getOpenWorldBounds();
        if (box == null) return cells;
        for (int x = MathHelper.floor_double(box.minX); x < box.maxX; x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < box.maxZ; z++) {
                if (x == xCoord && z == zCoord) continue;
                for (int y = yCoord; y <= yCoord + 1; y++) cells.add(new ChunkCoordinates(x, y, z));
            }
        }
        return cells;
    }

    private CustomDoorTileEntity getCollisionPartner() {
        Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
        return block instanceof CustomDoor ? ((CustomDoor) block).getPairedDoor(worldObj, this) : null;
    }

    private boolean canReserveCollisionHelpers() {
        CustomDoorTileEntity partner = getCollisionPartner();
        for (ChunkCoordinates cell : getHelperCells()) {
            int x = cell.posX, y = cell.posY, z = cell.posZ;
            if (!worldObj.blockExists(x, y, z)) return false;
            Block block = worldObj.getBlock(x, y, z);
            if (block == Blocks.air) continue;
            // Overlapping double leaves share the partner's original block cells and external helpers.
            if (partner != null && block instanceof CustomDoor
                && x == partner.xCoord
                && z == partner.zCoord
                && (y == partner.yCoord || y == partner.yCoord + 1)) continue;
            TileEntity tile = worldObj.getTileEntity(x, y, z);
            if (block == MalisisDoors.Blocks.customDoorCollision && tile instanceof CustomDoorCollisionTileEntity helper) {
                if (helper.belongsTo(this) || partner != null && helper.belongsTo(partner)) continue;
            }
            AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(worldObj, x, y, z);
            if (box == null || box.minX > x
                || box.minY > y
                || box.minZ > z
                || box.maxX < x + 1
                || box.maxY < y + 1
                || box.maxZ < z + 1) return false;
        }
        return true;
    }

    private boolean areCollisionChunksLoaded() {
        return worldObj.checkChunksExist(xCoord - 1, yCoord, zCoord - 1, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    private void updateCollisionHelpers() {
        if (worldObj == null || worldObj.isRemote) return;
        if (!needsCollisionHelpers()) {
            removeCollisionHelpers();
            return;
        }
        if (!areCollisionChunksLoaded()) return;
        for (ChunkCoordinates cell : getHelperCells()) {
            int x = cell.posX, y = cell.posY, z = cell.posZ;
            if (!worldObj.blockExists(x, y, z) || worldObj.getBlock(x, y, z) != Blocks.air) continue;
            if (worldObj.setBlock(x, y, z, MalisisDoors.Blocks.customDoorCollision, 0, 2)) {
                TileEntity tile = worldObj.getTileEntity(x, y, z);
                if (tile instanceof CustomDoorCollisionTileEntity)
                    ((CustomDoorCollisionTileEntity) tile).setOwner(this);
            }
        }
    }

    public void removeCollisionHelpers() {
        if (worldObj == null) return;
        for (int x = xCoord - 1; x <= xCoord + 1; x++) {
            for (int z = zCoord - 1; z <= zCoord + 1; z++) {
                if (x == xCoord && z == zCoord) continue;
                for (int y = yCoord; y <= yCoord + 1; y++) {
                    if (!worldObj.blockExists(x, y, z)
                        || worldObj.getBlock(x, y, z) != MalisisDoors.Blocks.customDoorCollision) continue;
                    TileEntity tile = worldObj.getTileEntity(x, y, z);
                    if (tile instanceof CustomDoorCollisionTileEntity
                        && ((CustomDoorCollisionTileEntity) tile).belongsTo(this)) worldObj.setBlockToAir(x, y, z);
                }
            }
        }
    }

    @Override
    public void setDoorState(DoorState newState) {
        if (worldObj != null && !worldObj.isRemote && newState == DoorState.OPENING) {
            if (!canReserveCollisionHelpers()) return;
            CustomDoorTileEntity partner = getCollisionPartner();
            if (partner != null && !partner.canReserveCollisionHelpers()) return;
        }
        super.setDoorState(newState);
        updateCollisionHelpers();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote || removing) return;
        if (needsCollisionHelpers() && !areCollisionChunksLoaded()) return;
        if (needsCollisionHelpers() && !canReserveCollisionHelpers()) {
            setDoorState(DoorState.CLOSING);
        }
        if (needsCollisionHelpers()) updateCollisionHelpers();
    }

    private Block frame;
    private Block topMaterial;
    private Block bottomMaterial;

    private int frameMetadata;
    private int topMaterialMetadata;
    private int bottomMaterialMetadata;

    // #region Getters/setters
    public Block getFrame() {
        return frame;
    }

    public Block getTopMaterial() {
        return topMaterial;
    }

    public Block getBottomMaterial() {
        return bottomMaterial;
    }

    public int getFrameMetadata() {
        return frameMetadata;
    }

    public int getTopMaterialMetadata() {
        return topMaterialMetadata;
    }

    public int getBottomMaterialMetadata() {
        return bottomMaterialMetadata;
    }

    // #end Getters/setters

    @Override
    public void onBlockPlaced(Door door, ItemStack itemStack) {
        super.onBlockPlaced(door, itemStack);

        NBTTagCompound nbt = itemStack.stackTagCompound;

        frame = Block.getBlockById(nbt.getInteger("frame"));
        topMaterial = Block.getBlockById(nbt.getInteger("topMaterial"));
        bottomMaterial = Block.getBlockById(nbt.getInteger("bottomMaterial"));

        frameMetadata = nbt.getInteger("frameMetadata");
        topMaterialMetadata = nbt.getInteger("topMaterialMetadata");
        bottomMaterialMetadata = nbt.getInteger("bottomMaterialMetadata");

        setCentered(shouldCenter());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        frame = Block.getBlockById(nbt.getInteger("frame"));
        topMaterial = Block.getBlockById(nbt.getInteger("topMaterial"));
        bottomMaterial = Block.getBlockById(nbt.getInteger("bottomMaterial"));

        frameMetadata = nbt.getInteger("frameMetadata");
        topMaterialMetadata = nbt.getInteger("topMaterialMetadata");
        bottomMaterialMetadata = nbt.getInteger("bottomMaterialMetadata");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("frame", Block.getIdFromBlock(frame));
        nbt.setInteger("topMaterial", Block.getIdFromBlock(topMaterial));
        nbt.setInteger("bottomMaterial", Block.getIdFromBlock(bottomMaterial));

        nbt.setInteger("frameMetadata", frameMetadata);
        nbt.setInteger("topMaterialMetadata", topMaterialMetadata);
        nbt.setInteger("bottomMaterialMetadata", bottomMaterialMetadata);
    }
}
