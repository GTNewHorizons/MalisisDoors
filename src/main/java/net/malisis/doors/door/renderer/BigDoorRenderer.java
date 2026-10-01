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

package net.malisis.doors.door.renderer;

import java.util.IdentityHashMap;
import java.util.Map;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.Vector;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.CollisionHelperBlock;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.malisis.doors.door.tileentity.MultiTile;
import net.malisis.doors.renderer.CopiedBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class BigDoorRenderer extends CopiedBlockRenderer {

    private MalisisModel model;
    private Shape frame;
    private Shape doorLeft;
    private Shape doorRight;
    private final AnimationRenderer ar = new AnimationRenderer();
    private BigDoorTileEntity tileEntity;

    private ForgeDirection direction;
    private Vector faceNormal;
    private final Map<Vertex, double[]> damageUVs = new IdentityHashMap<>();

    public BigDoorRenderer() {
        getBlockDamage = true;
    }

    @Override
    protected void initialize() {
        ResourceLocation rl = new ResourceLocation(MalisisDoors.modid, "models/big_door.obj");
        model = new MalisisModel(rl);
        frame = model.getShape("Frame");
        doorLeft = model.getShape("Left");
        doorRight = model.getShape("Right");

        for (Shape shape : model) {
            for (Face face : shape.getFaces()) {
                Vector normal = face.calculateNormal(null);
                boolean side = Math.abs(normal.x) > Math.abs(normal.z);
                boolean horizontal = Math.abs(normal.y) > Math.max(Math.abs(normal.x), Math.abs(normal.z));
                for (Vertex vertex : face.getVertexes()) {
                    double depth = (vertex.getZ() - (1 - Door.DOOR_WIDTH)) / Door.DOOR_WIDTH;
                    double u = side && !horizontal ? depth : vertex.getX() / 4;
                    double v = horizontal ? depth : 1 - vertex.getY() / 5;
                    damageUVs.put(vertex, new double[] { u, v });
                }
            }
        }

        rp = new RenderParameters();
        rp.useBlockBounds.set(false);
    }

    @Override
    public void render() {
        if (super.tileEntity == null) return;

        tileEntity = (BigDoorTileEntity) super.tileEntity;
        direction = Door.intToDir(tileEntity.getDirection());
        setup();

        if (renderType == RenderType.ISBRH_WORLD) {
            getBlockDamage = true;
            renderBlock();
        } else if (renderType == RenderType.TESR_WORLD) renderTileEntity();
    }

    @Override
    public void reset() {
        super.reset();
        tileEntity = null;
    }

    private void renderBlock() {
        BlockState state = tileEntity.getFrameState();
        if (!state.getBlock()
            .canRenderInPass(BigDoor.renderPass)) return;

        set(state.getBlock(), state.getMetadata());
        // rp.icon.set(state.getBlock().getIcon(1, state.getMetadata()));
        rp.icon.reset();
        rp.useWorldSensitiveIcon.set(false);
        drawShape(frame, rp);
    }

    private void renderTileEntity() {
        ar.setStartTime(
            tileEntity.getTimer()
                .getStart());

        if (tileEntity.getMovement() != null && (tileEntity.isMoving() || tileEntity.isOpened())) {
            Animation[] anims = tileEntity.getMovement()
                .getAnimations(tileEntity, model, rp);
            ar.animate(anims);
        }

        next(GL11.GL_POLYGON);
        rp.icon.reset();
        drawShape(doorLeft, rp);
        drawShape(doorRight, rp);
    }

    private void setup() {
        model.resetState();
        if (direction == ForgeDirection.SOUTH) model.rotate(180, 0, 1, 0, 0, 0, 0);
        else if (direction == ForgeDirection.EAST) model.rotate(-90, 0, 1, 0, 0, 0, 0);
        else if (direction == ForgeDirection.WEST) model.rotate(90, 0, 1, 0, 0, 0, 0);

    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        if (overrideTexture == null) {
            super.applyTexture(shape, parameters);
            return;
        }
        for (Face face : shape.getFaces()) {
            for (Vertex vertex : face.getVertexes()) {
                double[] uv = damageUVs.get(vertex);
                vertex
                    .setUV(overrideTexture.getInterpolatedU(uv[0] * 16), overrideTexture.getInterpolatedV(uv[1] * 16));
            }
        }
    }

    @Override
    protected void drawFace(Face face, RenderParameters faceParams, Tessellator tess) {
        faceNormal = face.calculateNormal(null);
        faceParams.colorFactor.set(
            (float) (faceNormal.x * faceNormal.x * 0.6 + faceNormal.y * (faceNormal.y * 3 + 1) / 4
                + faceNormal.z * faceNormal.z * 0.8));
        super.drawFace(face, faceParams, tess);
    }

    @Override
    protected int calcVertexBrightness(Vertex vertex, int[][] aoMatrix) {
        if (world == null || (renderType != RenderType.ISBRH_WORLD && renderType != RenderType.TESR_WORLD))
            return super.calcVertexBrightness(vertex, aoMatrix);

        if (block.getLightValue(world, x, y, z) != 0) return super.calcVertexBrightness(vertex, aoMatrix);

        double sampleX = x + vertex.getX() + faceNormal.x * 0.5 - 0.5;
        double sampleY = y + vertex.getY() + faceNormal.y * 0.5 - 0.5;
        double sampleZ = z + vertex.getZ() + faceNormal.z * 0.5 - 0.5;
        int blockX = MathHelper.floor_double(sampleX);
        int blockY = MathHelper.floor_double(sampleY);
        int blockZ = MathHelper.floor_double(sampleZ);
        double fractionX = sampleX - blockX;
        double fractionY = sampleY - blockY;
        double fractionZ = sampleZ - blockZ;
        double skyLight = 0;
        double blockLight = 0;
        double totalWeight = 0;
        for (int dx = 0; dx <= 1; dx++) {
            double weightX = dx == 0 ? 1 - fractionX : fractionX;
            for (int dy = 0; dy <= 1; dy++) {
                double weightY = dy == 0 ? 1 - fractionY : fractionY;
                for (int dz = 0; dz <= 1; dz++) {
                    double weight = weightX * weightY * (dz == 0 ? 1 - fractionZ : fractionZ);
                    if (weight == 0) continue;
                    int lightX = blockX + dx;
                    int lightY = blockY + dy;
                    int lightZ = blockZ + dz;
                    Block sampleBlock = world.getBlock(lightX, lightY, lightZ);
                    // Opaque cells belonging to the closed door are not surface-light samples.
                    if ((sampleBlock instanceof BigDoor || sampleBlock instanceof CollisionHelperBlock)
                        && sampleBlock.getLightOpacity(world, lightX, lightY, lightZ) != 0) continue;
                    int brightness = getMixedBrightnessForBlock(world, lightX, lightY, lightZ);
                    totalWeight += weight;
                    skyLight += (brightness >> 16 & 255) * weight;
                    blockLight += (brightness & 255) * weight;
                }
            }
        }
        if (totalWeight == 0) return 0;
        return (int) Math.round(skyLight / totalWeight) << 16 | (int) Math.round(blockLight / totalWeight);
    }

    @Override
    protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp) {
        int damageX = dbp.getPartialBlockX();
        int damageY = dbp.getPartialBlockY();
        int damageZ = dbp.getPartialBlockZ();
        if (damageX == x && damageY == y && damageZ == z) return true;
        if (!(world.getBlock(damageX, damageY, damageZ) instanceof CollisionHelperBlock)) return false;
        TileEntity damagedTile = world.getTileEntity(damageX, damageY, damageZ);
        return damagedTile instanceof MultiTile part && part.mainBlockSet
            && part.mainBlockX == x
            && part.mainBlockY == y
            && part.mainBlockZ == z;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }
}
