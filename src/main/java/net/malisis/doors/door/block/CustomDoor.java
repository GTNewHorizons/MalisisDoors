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

package net.malisis.doors.door.block;

import java.util.ArrayList;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class CustomDoor extends Door {

    public CustomDoor() {
        super(Material.wood);
        setBlockName("custom_door");
        setHardness(3.0F);
        setStepSound(soundTypeWood);
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {}

    @Override
    public IIcon getIcon(int side, int metadata) {
        return null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB bounds = null;
        for (AxisAlignedBB box : getBoundingBox(world, x, y, z, BoundingBoxType.COLLISION)) {
            if (box != null) bounds = bounds == null ? box : bounds.func_111270_a(box);
        }
        return bounds == null ? null : bounds.offset(x, y, z);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        if ((metadata & FLAG_TOPBLOCK) != 0) return null;

        return new CustomDoorTileEntity();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        DoorTileEntity te = Door.getDoor(world, x, y, z);
        if (!(te instanceof CustomDoorTileEntity)) return null;

        return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int metadata, EntityPlayer player) {}

    @SuppressWarnings("deprecation")
    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        DoorTileEntity tile = Door.getDoor(world, x, y, z);
        if (!(tile instanceof CustomDoorTileEntity)) return super.removedByPlayer(world, player, x, y, z);
        removeDoors(world, (CustomDoorTileEntity) tile, !player.capabilities.isCreativeMode);
        return true;
    }

    public CustomDoorTileEntity getPairedDoor(World world, CustomDoorTileEntity door) {
        if (door.getDescriptor() == null || !door.getDescriptor()
            .isDoubleDoor()) return null;
        ForgeDirection facing = Door.intToDir(door.getDirection());
        int sign = door.isReversed() ? 1 : -1;
        int x = door.xCoord - facing.offsetZ * sign;
        int z = door.zCoord + facing.offsetX * sign;
        if (!world.blockExists(x, door.yCoord, z) || world.getBlock(x, door.yCoord, z) != this) return null;
        TileEntity tile = world.getTileEntity(x, door.yCoord, z);
        if (!(tile instanceof CustomDoorTileEntity partner)) return null;
        return !partner.isRemoving() && partner.getDescriptor() != null
            && partner.getDescriptor()
                .isDoubleDoor()
            && partner.getDirection() == door.getDirection()
            && partner.isReversed() != door.isReversed()
            && partner.getMovement() == door.getMovement() ? partner : null;
    }

    private void removeDoors(World world, CustomDoorTileEntity door, boolean dropItems) {
        if (door.isRemoving()) return;
        CustomDoorTileEntity partner = getPairedDoor(world, door);
        CustomDoorTileEntity[] doors = partner == null ? new CustomDoorTileEntity[] { door }
            : new CustomDoorTileEntity[] { door, partner };
        ItemStack[] drops = new ItemStack[doors.length];
        for (int i = 0; i < doors.length; i++) {
            if (dropItems && !world.isRemote) drops[i] = CustomDoorItem.fromTileEntity(doors[i]);
        }
        for (CustomDoorTileEntity leaf : doors) leaf.beginRemoval();
        for (CustomDoorTileEntity leaf : doors) leaf.removeCollisionHelpers();
        for (int i = 0; i < doors.length; i++) {
            CustomDoorTileEntity leaf = doors[i];
            int x = leaf.xCoord, y = leaf.yCoord, z = leaf.zCoord;
            if (world.getBlock(x, y + 1, z) == this && (world.getBlockMetadata(x, y + 1, z) & FLAG_TOPBLOCK) != 0)
                world.setBlockToAir(x, y + 1, z);
            if (world.getBlock(x, y, z) == this && world.getTileEntity(x, y, z) == leaf) world.setBlockToAir(x, y, z);
            if (drops[i] != null) dropBlockAsItem(world, x, y, z, drops[i]);
        }
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int metadata) {
        DoorTileEntity tile = Door.getDoor(world, x, y, z);
        if (tile instanceof CustomDoorTileEntity) removeDoors(world, (CustomDoorTileEntity) tile, true);
        super.onBlockPreDestroy(world, x, y, z, metadata);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        DoorTileEntity tile = Door.getDoor(world, x, y, z);
        if (tile instanceof CustomDoorTileEntity door) {
            if (door.isRemoving()) return;
            if (world.getBlock(door.xCoord, door.yCoord + 1, door.zCoord) != this
                || !World.doesBlockHaveSolidTopSurface(world, door.xCoord, door.yCoord - 1, door.zCoord)) {
                removeDoors(world, door, true);
                return;
            }
        }
        super.onNeighborBlockChange(world, x, y, z, neighbor);
    }

    @Override
    protected ItemStack getDoorItemStack(IBlockAccess world, int x, int y, int z) {
        DoorTileEntity te = Door.getDoor(world, x, y, z);
        if (!(te instanceof CustomDoorTileEntity)) return null;
        return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
        int x = target.blockX;
        int y = target.blockY;
        int z = target.blockZ;

        CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
        if (te == null) return true;

        Block[] blocks = { te.getFrame(), te.getTopMaterial(), te.getBottomMaterial() };
        int[] metadata = { te.getFrameMetadata(), te.getTopMaterialMetadata(), te.getBottomMaterialMetadata() };

        ForgeDirection side = ForgeDirection.getOrientation(target.sideHit);

        double fxX = x + world.rand.nextDouble();
        double fxY = y + world.rand.nextDouble();
        double fxZ = z + world.rand.nextDouble();

        switch (side) {
            case DOWN:
                fxY = y + getBlockBoundsMinY() - 0.1F;
                break;
            case UP:
                fxY = y + getBlockBoundsMaxY() + 0.1F;
                break;
            case NORTH:
                fxZ = z + getBlockBoundsMinZ() - 0.1F;
                break;
            case SOUTH:
                fxZ = z + getBlockBoundsMaxY() + 0.1F;
                break;
            case EAST:
                fxX = x + getBlockBoundsMaxX() + 0.1F;
                break;
            case WEST:
                fxX = x + getBlockBoundsMinX() + 0.1F;
                break;
            default:
                break;
        }

        int i = world.rand.nextInt(blocks.length);
        if (blocks[i] == null) blocks[i] = Blocks.planks;

        EntityDiggingFX fx = new EntityDiggingFX(world, fxX, fxY, fxZ, 0.0D, 0.0D, 0.0D, blocks[i], metadata[i]);
        fx.multiplyVelocity(0.2F)
            .multipleParticleScaleBy(0.6F);
        effectRenderer.addEffect(fx);

        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
        byte nb = 4;
        EntityDiggingFX fx;

        CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
        if (te == null) return true;

        Block[] blocks = { te.getFrame(), te.getTopMaterial(), te.getBottomMaterial() };
        int[] metadata = { te.getFrameMetadata(), te.getTopMaterialMetadata(), te.getBottomMaterialMetadata() };

        for (int i = 0; i < nb; ++i) {
            for (int j = 0; j < nb; ++j) {
                for (int k = 0; k < nb; ++k) {
                    double fxX = x + (i + 0.5D) / nb;
                    double fxY = y + (j + 0.5D) / nb;
                    double fxZ = z + (k + 0.5D) / nb;
                    int l = (i + j + k) % 2;
                    if (blocks[l] == null) blocks[l] = Blocks.planks;
                    fx = new EntityDiggingFX(
                        world,
                        fxX,
                        fxY,
                        fxZ,
                        fxX - x - 0.5D,
                        fxY - y - 0.5D,
                        fxZ - z - 0.5D,
                        blocks[l],
                        metadata[l]);
                    effectRenderer.addEffect(fx);
                }
            }
        }

        return true;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
        if (te == null || te.getFrame() == null) return 0;

        return Math.max(
            Math.max(
                te.getFrame()
                    .getLightValue(),
                te.getTopMaterial()
                    .getLightValue()),
            te.getBottomMaterial()
                .getLightValue());
    }
}
