package net.malisis.doors.compat;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.malisis.core.util.BlockState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.door.tileentity.ForcefieldTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizons.angelica.rendering.BlockMaterialAttribute;
import com.gtnewhorizons.angelica.rendering.StateAwareTessellator;

public final class AngelicaCompatibility extends RenderCompatibility {

    public static void initialize() {
        RenderCompatibility.instance = new AngelicaCompatibility();
    }

    @Override
    public boolean isShadersActive() {
        return IrisApi.getInstance()
            .isShaderPackInUse();
    }

    @Override
    public boolean isTerrainTileEntity(TileEntity tileEntity) {
        return isShadersActive()
            && ((tileEntity instanceof DoorTileEntity && !(tileEntity instanceof ForcefieldTileEntity))
                || tileEntity instanceof BigDoorTileEntity
                || tileEntity instanceof VanishingTileEntity);
    }

    @Override
    public Scope beginTileEntity(TileEntity tileEntity) {
        if (!isTerrainTileEntity(tileEntity)) return NO_OP;

        Block material = tileEntity.getBlockType();
        int metadata = tileEntity.getBlockMetadata();
        if (material == MalisisDoors.Blocks.camoFenceGate && tileEntity instanceof FenceGateTileEntity
            && tileEntity.getWorldObj() != null) {
            BlockState camo = ((FenceGateTileEntity) tileEntity).getCamoState();
            if (camo != null && camo.getBlock() != material) {
                material = camo.getBlock();
                metadata = camo.getMetadata();
            }
        }

        CapturedRenderingState state = CapturedRenderingState.INSTANCE;
        state.pushCurrentBlockEntity();
        state.setCurrentBlockEntity(0);
        GbufferPrograms.pushOverridePhase(
            tileEntity instanceof BigDoorTileEntity ? WorldRenderingPhase.TERRAIN_CUTOUT
                : BlockMaterialAttribute.renderingPhase(material, false));
        BlockMaterialAttribute.set(material, metadata);
        return () -> {
            BlockMaterialAttribute.reset();
            GbufferPrograms.popOverridePhase();
            state.popCurrentBlockEntity();
        };
    }

    @Override
    public float directionalShade(float factor) {
        return BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading() ? 1.0F : factor;
    }

    @Override
    public int materialRenderPass(Block material, int original) {
        if (material == null || !isShadersActive()) return original;
        final var overrides = BlockRenderingSettings.INSTANCE.getBlockTypeIds();
        final var layer = overrides == null ? null : overrides.get(material);
        return layer == null ? original : layer.toVanillaPass();
    }

    @Override
    public MaterialBatch beginMaterials(Runnable flush, Block enclosingBlock, int enclosingMetadata) {
        if (!isShadersActive()) return NO_MATERIALS;
        return new MaterialBatch() {

            private Block currentBlock = enclosingBlock;
            private int currentMetadata = enclosingMetadata;
            private WorldRenderingPhase currentPhase = GbufferPrograms.getCurrentPhase();
            private boolean phasePushed;

            @Override
            public void setMaterial(Block block, int metadata) {
                WorldRenderingPhase phase = BlockMaterialAttribute.renderingPhase(block, false);
                if (block == currentBlock && metadata == currentMetadata && phase == currentPhase) return;
                flush.run();
                if (phase != currentPhase) {
                    if (phasePushed) GbufferPrograms.popOverridePhase();
                    GbufferPrograms.pushOverridePhase(phase);
                    phasePushed = true;
                    currentPhase = phase;
                }
                BlockMaterialAttribute.set(block, metadata);
                currentBlock = block;
                currentMetadata = metadata;
            }

            @Override
            public void close() {
                try {
                    flush.run();
                } finally {
                    BlockMaterialAttribute.set(enclosingBlock, enclosingMetadata);
                    if (phasePushed) GbufferPrograms.popOverridePhase();
                }
            }
        };
    }

    @Override
    public Scope beginChunkMaterial(Block block, int metadata) {
        if (!isShadersActive()) return NO_OP;
        StateAwareTessellator tessellator = (StateAwareTessellator) TessellatorManager.get();
        short previousId = tessellator.angelica$getShaderOverrideBlockId();
        int materialId = BlockMaterialAttribute.blockMaterialId(block, metadata);
        tessellator.angelica$setShaderOverrideBlockId(
            materialId == -1 ? StateAwareTessellator.UNMAPPED_SHADER_BLOCK_ID : (short) materialId);
        return () -> tessellator.angelica$setShaderOverrideBlockId(previousId);
    }

    @Override
    public Scope beginCopiedShape(Runnable flush, Block block, int metadata, Block enclosingBlock,
        int enclosingMetadata, boolean fading) {
        if (!isShadersActive()) return NO_OP;
        StateAwareTessellator tessellator = (StateAwareTessellator) TessellatorManager.get();
        return tessellator.angelica$isCeleritasMeshing() ? beginChunkMaterial(block, metadata)
            : beginMaterial(flush, block, metadata, enclosingBlock, enclosingMetadata, fading);
    }

    @Override
    public Scope beginMaterial(Runnable flush, Block block, int metadata, Block enclosingBlock, int enclosingMetadata,
        boolean fading) {
        if (!isShadersActive()) return NO_OP;
        flush.run();
        GbufferPrograms.pushOverridePhase(BlockMaterialAttribute.renderingPhase(block, fading));
        BlockMaterialAttribute.set(block, metadata);
        return () -> {
            try {
                flush.run();
            } finally {
                BlockMaterialAttribute.set(enclosingBlock, enclosingMetadata);
                GbufferPrograms.popOverridePhase();
            }
        };
    }

    @Override
    public Scope beginCopiedTileEntity() {
        if (!isShadersActive()) return NO_OP;
        BlockMaterialAttribute.reset();
        GbufferPrograms.pushOverridePhase(WorldRenderingPhase.BLOCK_ENTITIES);
        return GbufferPrograms::popOverridePhase;
    }
}
