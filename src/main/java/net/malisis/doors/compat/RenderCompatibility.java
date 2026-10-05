package net.malisis.doors.compat;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class RenderCompatibility {

    public static RenderCompatibility instance = new RenderCompatibility();

    public interface Scope extends AutoCloseable {

        @Override
        void close();
    }

    public interface MaterialBatch extends Scope {

        void setMaterial(Block block, int metadata);
    }

    protected static final Scope NO_OP = () -> {};
    protected static final MaterialBatch NO_MATERIALS = new MaterialBatch() {

        @Override
        public void setMaterial(Block block, int metadata) {}

        @Override
        public void close() {}
    };

    public boolean isShadersActive() {
        return false;
    }

    public boolean isTerrainTileEntity(TileEntity tileEntity) {
        return false;
    }

    public Scope beginTileEntity(TileEntity tileEntity) {
        return NO_OP;
    }

    public float directionalShade(float factor) {
        return factor;
    }

    public int materialRenderPass(Block material, int original) {
        return original;
    }

    public MaterialBatch beginMaterials(Runnable flush, Block enclosingBlock, int enclosingMetadata) {
        return NO_MATERIALS;
    }

    public Scope beginChunkMaterial(Block block, int metadata) {
        return NO_OP;
    }

    public Scope beginCopiedShape(Runnable flush, Block block, int metadata, Block enclosingBlock,
        int enclosingMetadata, boolean fading) {
        return NO_OP;
    }

    public Scope beginMaterial(Runnable flush, Block block, int metadata, Block enclosingBlock, int enclosingMetadata,
        boolean fading) {
        return NO_OP;
    }

    public Scope beginCopiedTileEntity() {
        return NO_OP;
    }
}
