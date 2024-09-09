package net.malisis.doors.renderer;

import net.malisis.core.util.ComplexAxisAlignedBoundingBox;
import net.malisis.doors.door.block.CollisionHelperBlock;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class CustomDoorBoundingBoxRenderer {

    public void renderOpenDoorBoundingBox(World world, CollisionHelperBlock block, EntityPlayer player,
        float partialTicks, MovingObjectPosition target) {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        float f1 = 0.002F;

        block.setBlockBoundsBasedOnState(world, target.blockX, target.blockY, target.blockZ);
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        ComplexAxisAlignedBoundingBox CAABB = block
            .getComplexBoundingBox(world, target.blockX, target.blockY, target.blockZ);

        CAABB.addOffset(-d0, -d1, -d2);

        renderComplexAABB(CAABB, -1);

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void renderComplexAABB(ComplexAxisAlignedBoundingBox CAABB, int color) {
        Tessellator tessellator = Tessellator.instance;
        if (color != -1) {
            tessellator.setColorOpaque_I(color);
        }
        renderHorizontalFaces(CAABB.flatSurfaces, tessellator);
        renderVerticals(CAABB.verticals, tessellator);
    }

    private static void renderVerticals(Pair<Vec3, Vec3>[] lines, Tessellator tessellator) {
        tessellator.startDrawing(1);
        for (Pair<Vec3, Vec3> line : lines) {
            tessellator.addVertex(line.getRight().xCoord, line.getRight().yCoord, line.getRight().zCoord);
            tessellator.addVertex(line.getLeft().xCoord, line.getLeft().yCoord, line.getLeft().zCoord);
        }
        tessellator.draw();
    }

    private static void renderHorizontalFaces(Vec3[][] faces, Tessellator tessellator) {
        for (Vec3[] face : faces) {
            tessellator.startDrawing(3);
            for (Vec3 vertex : face) {
                tessellator.addVertex(vertex.xCoord, vertex.yCoord, vertex.zCoord);
            }
            tessellator.addVertex(face[0].xCoord, face[0].yCoord, face[0].zCoord);
            tessellator.draw();
        }
    }

    public static void drawOutlineBoundingBoxWithMultipleBoxes(AxisAlignedBB AABB, int color) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);

        if (color != -1) {
            tessellator.setColorOpaque_I(color);
        }

        tessellator.addVertex(AABB.minX, AABB.minY, AABB.minZ);
        tessellator.addVertex(AABB.maxX, AABB.minY, AABB.minZ);
        tessellator.addVertex(AABB.maxX, AABB.minY, AABB.maxZ);
        tessellator.addVertex(AABB.minX, AABB.minY, AABB.maxZ);
        tessellator.addVertex(AABB.minX, AABB.minY, AABB.minZ);
        tessellator.draw();
    }
}
