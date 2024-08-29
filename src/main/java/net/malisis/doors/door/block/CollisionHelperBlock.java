package net.malisis.doors.door.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.CollisionHelperTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class CollisionHelperBlock extends BlockContainer implements ITileEntityProvider {


    BigDoor.Type type;

    // This class serves at the invisible collision blocks to help BigDoor with collisions
    public CollisionHelperBlock(BigDoor.Type type) {
        super(Material.wood);
        this.setHardness(1.0F);
        this.setStepSound(Block.soundTypeWood);
        this.setBlockTextureName(type.name + "_collisionHelper");
        this.type = type;
        this.setBlockName(type.name + "_collisionHelper");
    }

    @Override
    public boolean canDropFromExplosion(Explosion par1Explosion) {
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        if (side != 1) return false;

        ForgeDirection dir = ForgeDirection.getOrientation(side)
            .getOpposite();
        return world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)
            .isSideSolid(world, x, y, z, dir);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer EntityPlayer, int par6,
                                    float subx, float suby, float subz)
    {
        if (!world.isRemote)
        {
            final CollisionHelperTileEntity tileEntity = ((CollisionHelperTileEntity) world.getTileEntity(x, y, z));
            return tileEntity.onBlockActivated(world, x, y, z, EntityPlayer);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof CollisionHelperTileEntity) {
            ((CollisionHelperTileEntity) tileEntity).onBlockRemoval();
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        final int meta = world.getBlockMetadata(x, y, z);

        if (meta == 0)
        {
            this.setBlockBounds(1.0F - Door.DOOR_WIDTH, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
        if (meta == 1)
        {
            this.setBlockBounds(0.0F, 0.0F, 1.0F - Door.DOOR_WIDTH, 1.0F, 1.0F, 1.0F);
        }
        if (meta == 2)
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, Door.DOOR_WIDTH, 1.0F, 1.0F);
        }
        if (meta == 3)
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Door.DOOR_WIDTH);
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb,
                                        List<AxisAlignedBB> list, Entity entity) {
        setBlockBoundsBasedOnState(world, x, y, z);
        super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new CollisionHelperTileEntity();
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (tileEntity instanceof CollisionHelperTileEntity) {
            final int mainX = ((CollisionHelperTileEntity) tileEntity).mainBlockX;
            final int mainY = ((CollisionHelperTileEntity) tileEntity).mainBlockY;
            final int mainZ = ((CollisionHelperTileEntity) tileEntity).mainBlockZ;

            if (world.getTileEntity(x,y,z) != null) {
                return world.getBlock(mainX, mainY, mainZ).getBlockHardness(world, mainX, mainY, mainZ);
            }
        }

        return this.blockHardness;
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return false;
    }

    public void makeCollisionHelperBlock(World world, int x, int y, int z, int xMain, int yMain, int zMain, int meta)
    {
        world.setBlock(x, y, z, this, meta, 3);
        ((CollisionHelperTileEntity) world.getTileEntity(x, y, z)).setMainBlock(xMain, yMain, zMain);
    }
}
