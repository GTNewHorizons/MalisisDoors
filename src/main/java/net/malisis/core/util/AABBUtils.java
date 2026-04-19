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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class AABBUtils {

    public static enum Axis {
        X,
        Y,
        Z
    };

    private static int[] cos = { 1, 0, -1, 0 };
    private static int[] sin = { 0, 1, 0, -1 };

    public static AxisAlignedBB identity(BlockPos pos) {
        return identity(pos.getX(), pos.getY(), pos.getZ());
    }

    public static AxisAlignedBB identity(int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
    }

    public static AxisAlignedBB[] identities() {
        return identities(0, 0, 0);
    }

    public static AxisAlignedBB[] identities(int x, int y, int z) {
        return new AxisAlignedBB[] { identity(x, y, z) };
    }

    private static int getAngle(ForgeDirection dir) {
        switch (dir) {
            case EAST:
                return 1;
            case SOUTH:
                return 2;
            case WEST:
                return 3;
            case NORTH:
            default:
                return 0;
        }
    }

    /**
     * Rotate the {@link AxisAlignedBB} based on the specified direction.<br>
     * Assumes {@link ForgeDirection#NORTH} to be the default non rotated direction.<br>
     *
     *
     * @param aabb the aabb
     * @param dir  the dir
     * @return the axis aligned bb
     */
    public static AxisAlignedBB rotate(AxisAlignedBB aabb, ForgeDirection dir) {
        return rotate(aabb, getAngle(dir));
    }

    public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, ForgeDirection dir) {
        return rotate(aabbs, getAngle(dir));
    }

    public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, int angle) {
        for (AxisAlignedBB aabb : aabbs) rotate(aabb, angle);
        return aabbs;
    }

    public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle) {
        return rotate(aabb, angle, Axis.Y);
    }

    public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle, Axis axis) {
        if (aabb == null) return null;

        int a = angle % 4;
        if (a < 0) a += 4;
        int s = sin[a];
        int c = cos[a];

        AxisAlignedBB copy = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
        aabb.offset(-0.5F, -0.5F, -0.5F);
        copy.setBB(aabb);

        if (axis == Axis.X) {
            copy.minY = (aabb.minY * c) - (aabb.minZ * s);
            copy.maxY = (aabb.maxY * c) - (aabb.maxZ * s);
            copy.minZ = (aabb.minY * s) + (aabb.minZ * c);
            copy.maxZ = (aabb.maxY * s) + (aabb.maxZ * c);
        }
        if (axis == Axis.Y) {
            copy.minX = (aabb.minX * c) - (aabb.minZ * s);
            copy.maxX = (aabb.maxX * c) - (aabb.maxZ * s);
            copy.minZ = (aabb.minX * s) + (aabb.minZ * c);
            copy.maxZ = (aabb.maxX * s) + (aabb.maxZ * c);
        }

        if (axis == Axis.Z) {
            copy.minX = (aabb.minX * c) - (aabb.minY * s);
            copy.maxX = (aabb.maxX * c) - (aabb.maxY * s);
            copy.minY = (aabb.minX * s) + (aabb.minY * c);
            copy.maxY = (aabb.maxX * s) + (aabb.maxY * c);
        }

        aabb.setBB(fix(copy));
        aabb.offset(0.5F, 0.5F, 0.5F);

        return aabb;
    }

    public static AxisAlignedBB fix(AxisAlignedBB aabb) {
        double tmp;
        if (aabb.minX > aabb.maxX) {
            tmp = aabb.minX;
            aabb.minX = aabb.maxX;
            aabb.maxX = tmp;
        }

        if (aabb.minY > aabb.maxY) {
            tmp = aabb.minY;
            aabb.minY = aabb.maxY;
            aabb.maxY = tmp;
        }

        if (aabb.minZ > aabb.maxZ) {
            tmp = aabb.minZ;
            aabb.minZ = aabb.maxZ;
            aabb.maxZ = tmp;
        }

        return aabb;
    }

    public static AxisAlignedBB readFromNBT(NBTTagCompound tag, AxisAlignedBB aabb) {
        return aabb.setBounds(
            tag.getDouble("minX"),
            tag.getDouble("minY"),
            tag.getDouble("minZ"),
            tag.getDouble("maxX"),
            tag.getDouble("maxY"),
            tag.getDouble("maxZ"));
    }

    public static void writeToNBT(NBTTagCompound tag, AxisAlignedBB aabb) {
        if (aabb == null) return;
        tag.setDouble("minX", aabb.minX);
        tag.setDouble("minY", aabb.minY);
        tag.setDouble("minZ", aabb.minZ);
        tag.setDouble("maxX", aabb.maxX);
        tag.setDouble("maxY", aabb.maxY);
        tag.setDouble("maxZ", aabb.maxZ);
    }

    /**
     * Offsets the passed {@link AxisAlignedBB}s by the specified coordinates.
     *
     * @param x     the x
     * @param y     the y
     * @param z     the z
     * @param aabbs the aabbs
     */
    public static AxisAlignedBB[] offset(double x, double y, double z, AxisAlignedBB... aabbs) {
        return offset(new BlockPos(x, y, z), aabbs);
    }

    public static AxisAlignedBB[] offset(BlockPos pos, AxisAlignedBB... aabbs) {
        if (aabbs == null) return null;

        for (AxisAlignedBB aabb : aabbs) if (aabb != null) aabb.offset(pos.getX(), pos.getY(), pos.getZ());
        return aabbs;
    }

}
