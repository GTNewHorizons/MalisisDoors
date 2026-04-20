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

package net.malisis.core.util;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * @author Ordinastie
 *
 */
public class BlockState {

    protected BlockPos pos;
    protected Block block;
    protected int metadata;

    public BlockState(BlockPos pos, Block block, int metadata) {
        this.pos = pos;
        this.block = block;
        this.metadata = metadata;
    }

    public BlockState(int x, int y, int z, Block block, int metadata) {
        this(new BlockPos(x, y, z), block, metadata);
    }

    public BlockState(Block block, int metadata) {
        this(null, block, metadata);
    }

    public BlockState(Block block) {
        this(null, block, 0);
    }

    public BlockState(IBlockAccess world, BlockPos pos) {
        this(
            pos,
            world.getBlock(pos.getX(), pos.getY(), pos.getZ()),
            world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getBlock() {
        return block;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockState)) return false;

        BlockState bs = (BlockState) obj;
        return pos.equals(bs.pos) && block == bs.block && metadata == bs.metadata;
    }

    @Override
    public String toString() {
        return "[" + pos
            + "] "
            + (block != null ? block.getUnlocalizedName()
                .substring(5) + " ("
                + metadata
                + ")" : "");
    }

    public static BlockState fromNBT(NBTTagCompound nbt) {
        return fromNBT(nbt, "block", "metadata");
    }

    public static BlockState fromNBT(NBTTagCompound nbt, String blockName, String metadataName) {
        if (nbt == null) return null;

        Block block;
        if (nbt.hasKey(blockName, NBT.TAG_INT)) block = Block.getBlockById(nbt.getInteger(blockName));
        else block = Block.getBlockFromName(nbt.getString(blockName));

        if (block == null) return null;

        return new BlockState(block, nbt.getInteger(metadataName));
    }

    public static NBTTagCompound toNBT(NBTTagCompound nbt, BlockState state) {
        return toNBT(nbt, state, "block", "metadata");
    }

    public static NBTTagCompound toNBT(NBTTagCompound nbt, BlockState state, String blockName, String metadataName) {
        if (state == null) return nbt;

        nbt.setString(
            blockName,
            Block.blockRegistry.getNameForObject(state.getBlock())
                .toString());
        nbt.setInteger(metadataName, state.getMetadata());
        return nbt;
    }

}
