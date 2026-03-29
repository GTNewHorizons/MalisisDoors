package net.malisis.core.renderer.font;

import java.awt.Font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;

import com.gtnewhorizon.gtnhlib.util.font.FontRendering;
import com.gtnewhorizon.gtnhlib.util.font.IFontParameters;

/**
 * A slightly simpler alternative for {@link MinecraftFont} that uses vanilla and GTNHLib methods.
 * Incompatible with some settings in {@link FontRenderOptions}, but it doesn't look like the mod uses them anyway...
 */
public class VanillaFont extends MalisisFont {

    private final FontRenderer fontRenderer;

    public VanillaFont() {
        super((Font) null);
        this.textureRl = new ResourceLocation("textures/font/ascii.png");
        fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public float getCharWidth(char c) {
        IFontParameters fontParams = (IFontParameters) fontRenderer;
        return fontParams.getCharWidthFine(c);
    }

    @Override
    public float getCharWidth(char c, FontRenderOptions fro) {
        return this.getCharWidth(c);
    }

    @Override
    protected void drawString(String text, FontRenderOptions fro) {
        fontRenderer.drawString(text, 0, 0, fro.color, fro.shadow);
    }

    @Override
    public float getStringWidth(String str, FontRenderOptions fro, int start, int end) {
        if (StringUtils.isEmpty(str)) return 0;

        str = processString(str, null);
        if (start == 0 && end == 0) {
            return FontRendering.getStringWidth(str, fontRenderer);
        }
        String substr;
        if (end == 0) {
            substr = str.substring(start);
        } else {
            substr = str.substring(start, end);
        }
        return FontRendering.getStringWidth(substr, fontRenderer);
    }

    @Override
    public float getStringHeight(FontRenderOptions fro) {
        return fontRenderer.FONT_HEIGHT;
    }
}
