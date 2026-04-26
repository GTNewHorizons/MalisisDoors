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

import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class BlockPos {

    protected int x;
    protected int y;
    protected int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public BlockPos add(int x, int y, int z) {
        return new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    public BlockPos add(BlockPos pos) {
        if (pos == null) return new BlockPos(getX(), getY(), getZ());
        return add(pos.getX(), pos.getY(), pos.getZ());
    }

    // #region Moves

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos offset(ForgeDirection facing) {
        return this.offset(facing, 1);
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     *
     * @param facing The direction of the offset
     * @param n      The number of blocks to offset by
     */
    public BlockPos offset(ForgeDirection facing, int n) {
        return new BlockPos(
            this.getX() + facing.offsetX * n,
            this.getY() + facing.offsetY * n,
            this.getZ() + facing.offsetZ * n);
    }

    public BlockPos rotate(int rotation) {
        int[] cos = { 1, 0, -1, 0 };
        int[] sin = { 0, 1, 0, -1 };

        int a = rotation % 4;
        if (a < 0) a += 4;

        int newX = (x * cos[a]) - (z * sin[a]);
        int newZ = (x * sin[a]) + (z * cos[a]);

        return new BlockPos(newX, y, newZ);
    }

    // #end Moves

    public boolean isInRange(BlockPos pos, int range) {
        double x = pos.x - this.x;
        double y = pos.y - this.y;
        double z = pos.z - this.z;
        return (x * x + y * y + z * z) <= range * range;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof BlockPos)) return false;

        BlockPos pos = (BlockPos) obj;
        return this.getX() != pos.getX() ? false : (this.getY() != pos.getY() ? false : this.getZ() == pos.getZ());
    }

    @Override
    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

}
