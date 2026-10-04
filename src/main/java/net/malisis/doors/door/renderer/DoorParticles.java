package net.malisis.doors.door.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class DoorParticles {

    private DoorParticles() {}

    public static void addHitEffects(World world, MovingObjectPosition target, Block block,
        EffectRenderer effectRenderer) {
        if (target.hitVec == null) return;
        ForgeDirection side = ForgeDirection.getOrientation(target.sideHit);
        EntityDiggingFX particle = new EntityDiggingFX(
            world,
            target.hitVec.xCoord + side.offsetX * 0.1,
            target.hitVec.yCoord + side.offsetY * 0.1,
            target.hitVec.zCoord + side.offsetZ * 0.1,
            0,
            0,
            0,
            block,
            world.getBlockMetadata(target.blockX, target.blockY, target.blockZ));
        effectRenderer.addEffect(
            particle.applyColourMultiplier(target.blockX, target.blockY, target.blockZ)
                .multiplyVelocity(0.2F)
                .multipleParticleScaleBy(0.6F));
    }

    public static void addDestroyEffects(World world, int x, int y, int z, Block block, int metadata,
        EffectRenderer effectRenderer) {
        double centerX = x + 0.5;
        double centerY = y + 0.5;
        double centerZ = z + 0.5;
        MovingObjectPosition target = Minecraft.getMinecraft().objectMouseOver;
        if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
            && target.blockX == x
            && target.blockY == y
            && target.blockZ == z
            && target.hitVec != null) {
            centerX = target.hitVec.xCoord;
            centerY = target.hitVec.yCoord;
            centerZ = target.hitVec.zCoord;
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    double dx = (i + 0.5) / 4 - 0.5;
                    double dy = (j + 0.5) / 4 - 0.5;
                    double dz = (k + 0.5) / 4 - 0.5;
                    effectRenderer.addEffect(
                        new EntityDiggingFX(
                            world,
                            centerX + dx,
                            centerY + dy,
                            centerZ + dz,
                            dx,
                            dy,
                            dz,
                            block,
                            metadata).applyColourMultiplier(x, y, z));
                }
            }
        }
    }
}
