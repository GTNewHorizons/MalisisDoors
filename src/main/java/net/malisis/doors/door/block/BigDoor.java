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

import static net.minecraft.util.MathHelper.abs;
import static net.minecraft.util.Vec3.createVectorHelper;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.ComplexAxisAlignedBoundingBox;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoors.Items;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.malisis.doors.door.tileentity.IMultiBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Ordinastie
 *
 */
public class BigDoor extends MalisisBlock implements ITileEntityProvider {

    public enum Type {

        DEFAULT("carriage_door", net.minecraft.init.Items.wooden_door),
        CARRIAGE("carriage_door", net.minecraft.init.Items.wooden_door),
        MEDIEVAL("medieval_door", Items.doorSpruceItem);

        public String name;
        public Item door;

        private Type(String name, Item door) {
            this.name = name;
            this.door = door;
        }
    }

    private final Vec3[][] selectionOpenHoriztonalFaces = new Vec3[][] {
        { createVectorHelper(0, 5, 1), createVectorHelper(0, 5, 1 - Door.DOOR_WIDTH),
            createVectorHelper(4, 5, 1 - Door.DOOR_WIDTH), createVectorHelper(4, 5, 1) }, // bottom face
        { createVectorHelper(.5, 4, 1), createVectorHelper(.5, 4, 1 - Door.DOOR_WIDTH),
            createVectorHelper(3.5, 4, 1 - Door.DOOR_WIDTH), createVectorHelper(3.5, 4, 1) }, // top face
        { createVectorHelper(0, 4, 1 - Door.DOOR_WIDTH), createVectorHelper(0, 4, -.5), createVectorHelper(.5, 4, -.5),
            createVectorHelper(.5, 4, 1 - Door.DOOR_WIDTH) },
        { createVectorHelper(3.5, 4, 1 - Door.DOOR_WIDTH), createVectorHelper(4, 4, 1 - Door.DOOR_WIDTH),
            createVectorHelper(4, 4, -.5), createVectorHelper(3.5, 4, -.5) },
        { createVectorHelper(0, 0, 1), createVectorHelper(0, 0, -.5), createVectorHelper(.5, 0, -.5),
            createVectorHelper(.5, 0, 1) },
        { createVectorHelper(3.5, 0, 1), createVectorHelper(4, 0, 1), createVectorHelper(4, 0, -.5),
            createVectorHelper(3.5, 0, -.5) } };
    private final Pair<Vec3, Vec3>[] selectionOpenVerticals = new Pair[] {
        Pair.of(createVectorHelper(0, 0, 1), createVectorHelper(0, 5, 1)),
        Pair.of(createVectorHelper(4, 0, 1), createVectorHelper(4, 5, 1)),
        Pair.of(createVectorHelper(.5, 0, 1), createVectorHelper(.5, 4, 1)),
        Pair.of(createVectorHelper(3.5, 0, 1), createVectorHelper(3.5, 4, 1)),
        Pair.of(createVectorHelper(0, 0, -.5), createVectorHelper(0, 4, -.5)),
        Pair.of(createVectorHelper(.5, 0, -.5), createVectorHelper(.5, 4, -.5)),
        Pair.of(createVectorHelper(3.5, 0, -.5), createVectorHelper(3.5, 4, -.5)),
        Pair.of(createVectorHelper(4, 0, -.5), createVectorHelper(4, 4, -.5)),
        Pair.of(createVectorHelper(0, 4, 1 - Door.DOOR_WIDTH), createVectorHelper(0, 5, 1 - Door.DOOR_WIDTH)),
        Pair.of(createVectorHelper(4, 4, 1 - Door.DOOR_WIDTH), createVectorHelper(4, 5, 1 - Door.DOOR_WIDTH)) };

    public static int renderId;
    public static int renderPass = -1;
    private AxisAlignedBB defaultBoundingBox = AxisAlignedBB.getBoundingBox(0, 0, 1 - Door.DOOR_WIDTH, 4, 5, 1);
    private Type type;

    public BigDoor(Type type) {
        super(Material.wood);
        this.type = type;
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setBlockName(type.name);
        setCreativeTab(MalisisDoors.tab);
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon(MalisisDoors.modid + ":" + type.name);
    }

    @Override
    public String getItemIconName() {
        return MalisisDoors.modid + ":" + type.name + "_item";
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
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingEntity, ItemStack itemStack) {
        ForgeDirection dir = EntityUtils.getEntityFacing(livingEntity);
        int metadata = Door.dirToInt(dir);
        world.setBlockMetadataWithNotify(x, y, z, metadata, 2);

        BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
        if (te != null) {
            te.setFrameState(BlockState.fromNBT(itemStack.getTagCompound()));
            if (checkAreaClearForDoor(world, x, y, z, metadata)) {
                te.onCreate(x, y, z, metadata);
            } else {
                world.setBlockToAir(x, y, z);
                if (!world.isRemote && livingEntity instanceof EntityPlayerMP player) {
                    player.addChatMessage(new ChatComponentText("There's no room for the door!"));
                    if (!player.capabilities.isCreativeMode) {
                        final ItemStack doorBlock = new ItemStack(this, 1, 0);
                        final EntityItem entityitem = player.dropPlayerItemWithRandomChoice(doorBlock, false);
                        entityitem.delayBeforeCanPickup = 0;
                        entityitem.func_145797_a(player.getCommandSenderName());
                    }
                }
            }
        }
    }

    private boolean checkAreaClearForDoor(World world, int x, int y, int z, int meta) {
        boolean validSpot = true;
        boolean widthDirectionFlag = meta % 2 == 0;
        int xStep = meta == 3 ? -1 : 1;
        int zStep = meta == 0 ? -1 : 1;
        int xMax = widthDirectionFlag ? 1 : 4;
        int zMax = widthDirectionFlag ? 4 : 1;

        for (int yLoc = 0; yLoc < 5; yLoc++) {
            for (int xLoc = 0; abs(xLoc) < xMax; xLoc += xStep) {
                for (int zLoc = 0; abs(zLoc) < zMax; zLoc += zStep) {
                    if (!(yLoc == 0 && zLoc == 0 && xLoc == 0)) {
                        final Block potentialSpot = world.getBlock(x + xLoc, y + yLoc, z + zLoc);
                        if (!potentialSpot.getMaterial()
                            .isReplaceable()) {
                            validSpot = false;
                        }
                    }
                }
            }
        }
        return validSpot;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        final int buildHeight = world.getHeight() - 6; // No reason to have the door right at world height
        if (y > buildHeight) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof IMultiBlock) {
            ((IMultiBlock) tileEntity).onDestroy(tileEntity, meta);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) return true;
        BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
        if (te == null) return true;
        te.onBlockActivated(world, x, y, z, player);
        return true;
    }

    @Override
    public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type) {
        BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
        if (te == null) return AABBUtils.identities();

        // MalisisCore.message(te.getDirection());

        AxisAlignedBB[] aabbs = new AxisAlignedBB[] { defaultBoundingBox.copy() };
        if (type == BoundingBoxType.RENDER) {
            aabbs[0].minZ = -.5F;
        } else if ((type == BoundingBoxType.COLLISION || type == BoundingBoxType.CHUNKCOLLISION
            || type == BoundingBoxType.RAYTRACE
            || type == BoundingBoxType.SELECTION) && (te.isOpened() || te.isMoving())) {
                aabbs = new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox(0, 0, -0.5F, 0.5F, 4, 1),
                    AxisAlignedBB.getBoundingBox(3.5F, 0, -0.5F, 4, 4, 1),
                    AxisAlignedBB.getBoundingBox(0, 4, 1 - Door.DOOR_WIDTH, 4, 5, 1) };
            }

        return AABBUtils.rotate(aabbs, Door.intToDir(te.getDirection()));
    }

    public ComplexAxisAlignedBoundingBox getComplexBoundingBoxWithOffset(IBlockAccess world, int x, int y, int z,
        BoundingBoxType type) {
        BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, x, y, z);
        if (te == null) return ComplexAxisAlignedBoundingBox.defaultComplexBoundingBox;

        if (type == BoundingBoxType.SELECTION && (te.isOpened() || te.isMoving())) {
            Vec3[][] clonedFlatSurfaces = new Vec3[selectionOpenHoriztonalFaces.length][];
            for (int i = 0; i < selectionOpenHoriztonalFaces.length; i++) {
                clonedFlatSurfaces[i] = new Vec3[selectionOpenHoriztonalFaces[i].length];
                for (int j = 0; j < selectionOpenHoriztonalFaces[i].length; j++) {
                    Vec3 vec = selectionOpenHoriztonalFaces[i][j];
                    clonedFlatSurfaces[i][j] = createVectorHelper(vec.xCoord, vec.yCoord, vec.zCoord);
                }
            }

            Pair<Vec3, Vec3>[] clonedVerticals = new Pair[selectionOpenVerticals.length];
            for (int i = 0; i < selectionOpenVerticals.length; i++) {
                Vec3 left = selectionOpenVerticals[i].getLeft();
                Vec3 right = selectionOpenVerticals[i].getRight();
                clonedVerticals[i] = Pair.of(
                    createVectorHelper(left.xCoord, left.yCoord, left.zCoord),
                    createVectorHelper(right.xCoord, right.yCoord, right.zCoord));
            }

            ComplexAxisAlignedBoundingBox CAABB = new ComplexAxisAlignedBoundingBox(
                clonedFlatSurfaces,
                clonedVerticals);
            CAABB.rotate(Door.intToDir(te.getDirection()), ComplexAxisAlignedBoundingBox.Axis.Y);
            // Offset needed post-rotation
            switch (Door.intToDir(te.getDirection())) {
                case EAST -> CAABB.addOffset(x + 1, y, z);
                case NORTH -> CAABB.addOffset(x, y, z);
                case SOUTH -> CAABB.addOffset(x + 1, y, z + 1);
                case WEST -> CAABB.addOffset(x, y, z + 1);
            }
            return CAABB;
        }
        return ComplexAxisAlignedBoundingBox.defaultComplexBoundingBox;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new BigDoorTileEntity(this.type);
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
    public int getRenderType() {
        return renderId;
    }

    public BigDoor.Type getType() {
        return type;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(
            (float) defaultBoundingBox.minX,
            (float) defaultBoundingBox.minY,
            (float) defaultBoundingBox.minZ,
            (float) defaultBoundingBox.maxX,
            (float) defaultBoundingBox.maxY,
            (float) defaultBoundingBox.maxZ);
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean canRenderInPass(int pass) {
        renderPass = pass;
        return true;
    }
}
