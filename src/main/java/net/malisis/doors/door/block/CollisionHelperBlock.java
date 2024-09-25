package net.malisis.doors.door.block;

import java.util.List;
import java.util.Random;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.util.ComplexAxisAlignedBoundingBox;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.malisis.doors.door.tileentity.MultiTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CollisionHelperBlock extends BlockContainer implements ITileEntityProvider {

    public BigDoor.Type type;

    private IIcon[] fakeIcons;

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
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.fakeIcons = new IIcon[2];
        this.fakeIcons[0] = register.registerIcon(MalisisDoors.modid + ":" + BigDoor.Type.CARRIAGE);
        this.fakeIcons[1] = register.registerIcon(MalisisDoors.modid + ":" + BigDoor.Type.MEDIEVAL);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return switch (meta) {
            case 0 -> this.fakeIcons[0];
            case 1 -> this.fakeIcons[1];
            default -> this.fakeIcons[0];
        };
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer EntityPlayer, int par6, float subx,
        float suby, float subz) {
        final MultiTile tileEntity = ((MultiTile) world.getTileEntity(x, y, z));
        return tileEntity.onBlockActivated(world, x, y, z, EntityPlayer);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof MultiTile) {
            ((MultiTile) tileEntity).onBlockRemoval();
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        final int meta = world.getBlockMetadata(x, y, z);

        switch (meta) {
            case 0: // 3 Pixels wide on the east side of the block
                this.setBlockBounds(1.0F - Door.DOOR_WIDTH, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;
            case 1: // 3 Pixels wide on the south side of the block
                this.setBlockBounds(0.0F, 0.0F, 1.0F - Door.DOOR_WIDTH, 1.0F, 1.0F, 1.0F);
                break;
            case 2: // 3 Pixels wide on the west side of the block
                this.setBlockBounds(0.0F, 0.0F, 0.0F, Door.DOOR_WIDTH, 1.0F, 1.0F);
                break;
            case 3: // 3 Pixels wide on the north side of the block
                this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Door.DOOR_WIDTH);
                break;
            case 4: // 8 Pixels wide on the east side of the block
                this.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;
            case 5: // 8 Pixels wide on the south side of the block
                this.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
                break;
            case 6: // 8 Pixels wide on the west side of the block
                this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
                break;
            case 7: // 8 Pixels wide on the north side of the block
                this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
                break;
            case 8: // Quarter of a block on north-west corner of the block
                this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F);
                break;
            case 9: // Quarter of a block on north-east corner of the block
                this.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
                break;
            case 10: // Quarter of a block on south-east corner of the block
                this.setBlockBounds(0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
                break;
            case 11: // Quarter of a block on the south-west corner of the block
                this.setBlockBounds(0.0F, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F);
                break;
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
        MultiTile thisTE = ((MultiTile) world.getTileEntity(x, y, z));
        Block mainBlock = world.getBlock(thisTE.mainBlockX, thisTE.mainBlockY, thisTE.mainBlockZ);
        if (mainBlock instanceof BigDoor) {
            return mainBlock
                .getSelectedBoundingBoxFromPool(world, thisTE.mainBlockX, thisTE.mainBlockY, thisTE.mainBlockZ);
        }
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    public ComplexAxisAlignedBoundingBox getComplexBoundingBox(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        MultiTile TE = getTileEntity(world, x, y, z);
        BigDoor mainBlock = getMainBlock(world, x, y, z);
        return mainBlock.getComplexBoundingBoxWithOffset(
            world,
            TE.mainBlockX,
            TE.mainBlockY,
            TE.mainBlockZ,
            BoundingBoxType.SELECTION);
    }

    public BigDoor getMainBlock(World world, int x, int y, int z) {
        MultiTile cTE = this.getTileEntity(world, x, y, z);
        return (BigDoor) world.getBlock(cTE.mainBlockX, cTE.mainBlockY, cTE.mainBlockZ);
    }

    public BigDoorTileEntity getMainTileEntity(World world, int x, int y, int z) {
        MultiTile cTE = this.getTileEntity(world, x, y, z);
        return (BigDoorTileEntity) world.getTileEntity(cTE.mainBlockX, cTE.mainBlockY, cTE.mainBlockZ);
    }

    public MultiTile getTileEntity(World world, int x, int y, int z) {
        return (MultiTile) world.getTileEntity(x, y, z);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new MultiTile();
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof MultiTile) {
            final int mainX = ((MultiTile) tileEntity).mainBlockX;
            final int mainY = ((MultiTile) tileEntity).mainBlockY;
            final int mainZ = ((MultiTile) tileEntity).mainBlockZ;
            return world.getBlock(mainX, mainY, mainZ)
                .getBlockHardness(world, mainX, mainY, mainZ);
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

    public void makeCollisionHelperBlock(World world, int x, int y, int z, int meta, int xMain, int yMain, int zMain,
        int metaMain) {
        world.setBlock(x, y, z, this, meta, 3);
        MultiTile tile = (MultiTile) world.getTileEntity(x, y, z);
        tile.setMainBlock(xMain, yMain, zMain);
        tile.setMainBlockMeta(metaMain);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof MultiTile multiTileEntity) {
            if (multiTileEntity.mainBlockSet) {
                final Block mainBlock = world
                    .getBlock(multiTileEntity.mainBlockX, multiTileEntity.mainBlockY, multiTileEntity.mainBlockZ);
                if (Blocks.air != mainBlock) {
                    return mainBlock.getPickBlock(
                        target,
                        world,
                        multiTileEntity.mainBlockX,
                        multiTileEntity.mainBlockY,
                        multiTileEntity.mainBlockZ);
                }
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof MultiTile multiTileEntity) {
            if (multiTileEntity.mainBlockSet) {
                final Block mainBlock = world
                    .getBlock(multiTileEntity.mainBlockX, multiTileEntity.mainBlockY, multiTileEntity.mainBlockZ);
                if (Blocks.air != mainBlock) {
                    return world
                        .getBlock(multiTileEntity.mainBlockX, multiTileEntity.mainBlockY, multiTileEntity.mainBlockZ)
                        .addDestroyEffects(world, x, y, z, meta, effectRenderer);
                }
            }
        }
        return super.addDestroyEffects(world, x, y, z, meta, effectRenderer);
    }
}
