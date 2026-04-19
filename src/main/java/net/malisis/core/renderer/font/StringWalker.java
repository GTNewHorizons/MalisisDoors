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

package net.malisis.core.renderer.font;

import net.minecraft.util.EnumChatFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalker {

    private MalisisFont font;
    private FontRenderOptions fro;
    private String str;
    private boolean litteral;
    private boolean skipChars = true;
    private boolean applyStyles;

    private int index;
    private int endIndex;
    private char c;
    private EnumChatFormatting ecf;
    private float width;

    public StringWalker(String str, MalisisFont font, FontRenderOptions fro) {
        this.str = str;
        this.font = font;
        this.fro = fro;
        this.index = 0;
        this.endIndex = str.length();
        litteral = fro != null && fro.disableECF;
    }

    // #region Getters/Setters

    public void skipChars(boolean skip) {
        this.skipChars = skip;
    }

    public void applyStyles(boolean apply) {
        this.applyStyles = apply;
    }

    public int getIndex() {
        return index;
    }

    public char getChar() {
        return c;
    }

    public boolean isFormatting() {
        return ecf != null;
    }

    public float getWidth() {
        return width;
    }

    public void startIndex(int index) {
        this.index = index;
    }

    // #end Getters/Setters

    private void checkEcf() {
        ecf = FontRenderOptions.getFormatting(str, index);
        if (ecf == null) ecf = FontRenderOptions.getFormatting(str, index - 1);

        if (ecf == null) return;

        if (applyStyles && fro != null) fro.apply(ecf);

        if (skipChars && !litteral) {
            index += 2;
            checkEcf();
        }
    }

    public int walkTo(float x) {
        float width = 0;
        while (walk()) {
            width += getWidth();
            if (width > x) return getIndex() - 1;
        }

        return getIndex();
    }

    public boolean walk() {
        if (index >= endIndex) return false;

        checkEcf();
        // checkLink();

        if (index >= endIndex) return false;

        c = str.charAt(index);
        width = font.getCharWidth(c, fro);

        if (!litteral && !skipChars && ecf != null) width = 0;

        index++;
        return true;
    }
}
