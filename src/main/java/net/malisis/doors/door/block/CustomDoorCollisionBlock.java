package net.malisis.doors.door.block;

import java.util.ArrayList;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.tileentity.CustomDoorCollisionTileEntity;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CustomDoorCollisionBlock extends BlockContainer {

    public CustomDoorCollisionBlock() {
        super(Material.wood);
        setBlockName("custom_door");
        setHardness(3);
        setStepSound(soundTypeWood);
        setLightOpacity(0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new CustomDoorCollisionTileEntity();
    }

    private CustomDoorTileEntity getOwner(IBlockAccess world, int x, int y, int z) {
        TileEntity helper = world.getTileEntity(x, y, z);
        return helper instanceof CustomDoorCollisionTileEntity
            ? ((CustomDoorCollisionTileEntity) helper).getOwner(world)
            : null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        CustomDoorTileEntity owner = getOwner(world, x, y, z);
        if (owner == null || !owner.needsCollisionHelpers()
            || !owner.isHelperCell(x, y, z)
            || owner.isMoving()
            || !owner.isOpened()) return null;
        CustomDoor door = (CustomDoor) world.getBlock(owner.xCoord, owner.yCoord, owner.zCoord);
        for (AxisAlignedBB box : door.getBoundingBox(world, owner.xCoord, y, owner.zCoord, BoundingBoxType.COLLISION)) {
            if (box == null) continue;
            box.offset(owner.xCoord, y, owner.zCoord);
            double minX = Math.max(x, box.minX), minY = Math.max(y, box.minY), minZ = Math.max(z, box.minZ);
            double maxX = Math.min(x + 1, box.maxX), maxY = Math.min(y + 1, box.maxY), maxZ = Math.min(z + 1, box.maxZ);
            if (minX < maxX && minY < maxY && minZ < maxZ)
                return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return null;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        AxisAlignedBB box = getCollisionBoundingBoxFromPool(world, x, y, z);
        if (box == null) return null;
        MovingObjectPosition hit = box.calculateIntercept(start, end);
        return hit == null ? null : new MovingObjectPosition(x, y, z, hit.sideHit, hit.hitVec);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB box = getCollisionBoundingBoxFromPool(world, x, y, z);
        return box == null ? AxisAlignedBB.getBoundingBox(x, y, z, x, y, z) : box;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        CustomDoorTileEntity owner = getOwner(world, x, y, z);
        return owner != null && world.getBlock(owner.xCoord, owner.yCoord, owner.zCoord)
            .onBlockActivated(world, owner.xCoord, owner.yCoord, owner.zCoord, player, side, hitX, hitY, hitZ);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        CustomDoorTileEntity owner = getOwner(world, x, y, z);
        if (owner == null) return world.setBlockToAir(x, y, z);
        return world.getBlock(owner.xCoord, owner.yCoord, owner.zCoord)
            .removedByPlayer(world, player, owner.xCoord, owner.yCoord, owner.zCoord);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        CustomDoorTileEntity owner = getOwner(world, x, y, z);
        return owner == null ? null : CustomDoorItem.fromTileEntity(owner);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<>();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer renderer) {
        CustomDoorTileEntity owner = getOwner(world, target.blockX, target.blockY, target.blockZ);
        if (owner != null) {
            MovingObjectPosition doorHit = new MovingObjectPosition(
                owner.xCoord,
                target.blockY,
                owner.zCoord,
                target.sideHit,
                target.hitVec);
            world.getBlock(owner.xCoord, owner.yCoord, owner.zCoord)
                .addHitEffects(world, doorHit, renderer);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, int x, int y, int z, int metadata, EffectRenderer renderer) {
        CustomDoorTileEntity owner = getOwner(world, x, y, z);
        if (owner != null) world.getBlock(owner.xCoord, owner.yCoord, owner.zCoord)
            .addDestroyEffects(world, owner.xCoord, y, owner.zCoord, metadata, renderer);
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister register) {}

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return false;
    }
}
