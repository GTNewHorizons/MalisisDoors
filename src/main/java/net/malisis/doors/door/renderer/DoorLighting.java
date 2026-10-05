package net.malisis.doors.door.renderer;

import net.malisis.core.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

final class DoorLighting {

    private DoorLighting() {}

    static int sample(IBlockAccess world, int x, int y, int z, Vector normal) {
        int sampleX = MathHelper.floor_double(x + 0.5 + normal.x);
        int sampleY = MathHelper.floor_double(y + 0.5 + normal.y);
        int sampleZ = MathHelper.floor_double(z + 0.5 + normal.z);
        if (isSurfaceLightSample(world, sampleX, sampleY, sampleZ))
            return getBrightness(world, sampleX, sampleY, sampleZ);

        int skyLight = 0;
        int blockLight = 0;
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            sampleX = x + side.offsetX;
            sampleY = y + side.offsetY;
            sampleZ = z + side.offsetZ;
            if (!isSurfaceLightSample(world, sampleX, sampleY, sampleZ)) continue;
            int brightness = getBrightness(world, sampleX, sampleY, sampleZ);
            skyLight = Math.max(skyLight, brightness >> 16 & 255);
            blockLight = Math.max(blockLight, brightness & 255);
        }
        return skyLight << 16 | blockLight;
    }

    private static boolean isSurfaceLightSample(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block.getLightOpacity(world, x, y, z) < 255 || block.getLightValue(world, x, y, z) > 0;
    }

    private static int getBrightness(IBlockAccess world, int x, int y, int z) {
        return world.getBlock(x, y, z)
            .getMixedBrightnessForBlock(world, x, y, z);
    }
}
