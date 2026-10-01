package net.malisis.doors.renderer;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.doors.compat.RenderCompatibility;
import net.malisis.doors.compat.RenderCompatibility.Scope;

import org.lwjgl.opengl.GL11;

public abstract class CopiedBlockRenderer extends MalisisRenderer {

    @Override
    public void drawShape(Shape shape, RenderParameters parameters) {
        if (renderType != RenderType.ISBRH_WORLD || overrideTexture != null
            || !RenderCompatibility.instance.isShadersActive()) {
            super.drawShape(shape, parameters);
            return;
        }

        boolean fading = false;
        if (parameters != null && parameters.usePerVertexAlpha.get()) {
            for (Face face : shape.getFaces()) {
                for (Vertex vertex : face.getVertexes()) fading |= vertex.getAlpha() != 255;
            }
        }
        try (Scope ignored = RenderCompatibility.instance.beginCopiedShape(
            () -> next(GL11.GL_QUADS),
            block,
            blockMetadata,
            world.getBlock(x, y, z),
            world.getBlockMetadata(x, y, z),
            fading)) {
            super.drawShape(shape, parameters);
        }
    }
}
