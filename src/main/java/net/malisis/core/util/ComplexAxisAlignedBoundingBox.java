package net.malisis.core.util;

import static net.minecraft.util.Vec3.createVectorHelper;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

public class ComplexAxisAlignedBoundingBox {

    public enum Axis {
        X,
        Y,
        Z
    };

    private static final int[] cos = { 1, 0, -1, 0 };
    private static final int[] sin = { 0, 1, 0, -1 };

    public Vec3[][] flatSurfaces;
    public Pair<Vec3, Vec3>[] verticals;

    public static ComplexAxisAlignedBoundingBox defaultComplexBoundingBox = new ComplexAxisAlignedBoundingBox(
        new Vec3[][] {
            { createVectorHelper(0, 0, 0), createVectorHelper(0, 0, 1), createVectorHelper(1, 0, 1),
                createVectorHelper(1, 0, 0) }, // bottom face
            { createVectorHelper(0, 1, 0), createVectorHelper(0, 1, 1), createVectorHelper(1, 1, 1),
                createVectorHelper(1, 1, 0) }, // top face
        },
        new Pair[] { Pair.of(createVectorHelper(0, 0, 0), createVectorHelper(0, 1, 0)),
            Pair.of(createVectorHelper(1, 0, 0), createVectorHelper(1, 1, 0)),
            Pair.of(createVectorHelper(0, 0, 1), createVectorHelper(0, 1, 1)),
            Pair.of(createVectorHelper(1, 0, 1), createVectorHelper(1, 1, 1)) });

    public ComplexAxisAlignedBoundingBox(Vec3[][] flatSurfaces, Pair<Vec3, Vec3>[] verticals) {
        this.flatSurfaces = flatSurfaces;
        this.verticals = verticals;
    }

    public void addOffset(double x, double y, double z) {
        for (Vec3[] face : flatSurfaces) {
            for (Vec3 vec : face) {
                vec.xCoord += x;
                vec.yCoord += y;
                vec.zCoord += z;
            }
        }

        for (Pair<Vec3, Vec3> vertical : verticals) {
            Vec3 start = vertical.getLeft();
            Vec3 end = vertical.getRight();

            start.xCoord += x;
            start.yCoord += y;
            start.zCoord += z;

            end.xCoord += x;
            end.yCoord += y;
            end.zCoord += z;
        }
    }

    /**
     * Rotates the ComplexAxisAlignedBoundingBox by the given angle around the specified axis.
     */
    public void rotate(ForgeDirection dir, Axis axis) {
        int angle = getAngle(dir);
        // Rotate flat surfaces
        for (int i = 0; i < flatSurfaces.length; i++) {
            for (int j = 0; j < flatSurfaces[i].length; j++) {
                flatSurfaces[i][j] = rotateVec(flatSurfaces[i][j], angle, axis);
            }
        }

        // Rotate verticals
        for (int i = 0; i < verticals.length; i++) {
            Vec3 start = verticals[i].getLeft();
            Vec3 end = verticals[i].getRight();
            verticals[i] = Pair.of(rotateVec(start, angle, axis), rotateVec(end, angle, axis));
        }
    }

    private Vec3 rotateVec(Vec3 vec, int angle, Axis axis) {
        int a = angle % 4;
        if (a < 0) a += 4;
        int s = sin[a];
        int c = cos[a];

        double x = vec.xCoord;
        double y = vec.yCoord;
        double z = vec.zCoord;

        return switch (axis) {
            case X -> createVectorHelper(x, y * c - z * s, y * s + z * c);
            case Y -> createVectorHelper(x * c - z * s, y, x * s + z * c);
            case Z -> createVectorHelper(x * c - y * s, x * s + y * c, z);
        };
    }

    private static int getAngle(ForgeDirection dir) {
        return switch (dir) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }
}
