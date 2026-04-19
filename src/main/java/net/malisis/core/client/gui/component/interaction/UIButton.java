/*
 * The MIT License (MIT) Copyright (c) 2014 PaleoCrafter, Ordinastie Permission is hereby granted, free of charge, to
 * any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions: The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.font.VanillaFont;
import net.malisis.core.util.MouseButton;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent<UIButton> implements IGuiText<UIButton> {

    protected GuiIcon iconHovered;
    protected GuiIcon iconDisabled;
    protected GuiIcon iconPressed;

    protected VanillaFont font = VanillaFont.vanillaFont;
    /** The {@link FontRenderOptions} to use for this {@link UITooltip}. */
    protected FontRenderOptions fro = new FontRenderOptions();
    /** The {@link FontRenderOptions} to use for this {@link UITooltip} when hovered. */
    protected FontRenderOptions hoveredFro = new FontRenderOptions();
    /** Text used for this {@link UIButton}. Exclusive with {@link #image}. */
    protected String text;
    /** Image used for this {@link UIButton}. Exclusive with {@link #text}. */
    protected UIImage image;
    /** Whether the size of this {@link UIButton} is automatically calculated based on its contents. */
    protected boolean autoSize = true;
    /** Whether this {@link UIButton} is currently being pressed. */
    protected boolean isPressed = false;

    /** The background color of this {@link UIButton}. */
    protected int bgColor = 0xFFFFFF;
    /** Offset for the contents */
    protected int offsetX, offsetY;

    /**
     * Instantiates a new {@link UIButton}.
     *
     * @param gui the gui
     */
    public UIButton(MalisisGui gui) {
        super(gui);
        fro.color = 0xFFFFFF;
        fro.shadow = true;
        hoveredFro.color = 0xFFFFA0;
        hoveredFro.shadow = true;

        shape = new XYResizableGuiShape();
        icon = gui.getGuiTexture()
            .getXYResizableIcon(0, 20, 200, 20, 5);
        iconHovered = gui.getGuiTexture()
            .getXYResizableIcon(0, 40, 200, 20, 5);
        iconDisabled = gui.getGuiTexture()
            .getXYResizableIcon(0, 0, 200, 20, 5);
        iconPressed = (GuiIcon) gui.getGuiTexture()
            .getXYResizableIcon(0, 40, 200, 20, 5)
            .flip(true, true);
    }

    /**
     * Instantiates a new {@link UIButton}.
     *
     * @param gui  the gui
     * @param text the text
     */
    public UIButton(MalisisGui gui, String text) {
        this(gui);
        setText(text);
    }

    @Override
    public FontRenderOptions getFontRenderOptions() {
        return fro;
    }

    /**
     * Sets the text of this {@link UIButton}.
     *
     * @param text the text
     * @return this {@link UIButton}
     */
    public UIButton setText(String text) {
        this.text = text;
        setSize(width, height);
        image = null;
        return this;
    }

    /**
     * Sets the {@link UIImage} for this {@link UIButton}. If a width of 0 was previously set, it will be recalculated
     * for this image.
     *
     * @param image the image
     * @return this {@link UIButton}
     */
    public UIButton setImage(UIImage image) {
        this.image = image;
        image.setParent(this);
        setSize(width, height);
        text = null;
        return this;
    }

    /**
     * Sets the width of this {@link UIButton} with a default height of 20px.
     *
     * @param width the width
     * @return this {@link UIButton}
     */
    public UIButton setSize(int width) {
        return setSize(width, 20);
    }

    /**
     * Sets the size of this {@link UIButton}.
     *
     * @param width  the width
     * @param height the height
     * @return this {@link UIButton}
     */
    @Override
    public UIButton setSize(int width, int height) {
        if (autoSize) {
            if (image != null) {
                int w = image.getRawWidth();
                int h = image.getRawHeight();
                width = Math.max(width, w + 2);
                height = Math.max(height, h + 2);
            } else {
                int w = (int) font.getStringWidth(text, fro);
                int h = (int) font.getStringHeight(fro);
                width = Math.max(width, w + 6);
                height = Math.max(height, h + 6);
            }
        }

        this.width = width;
        this.height = height;

        return this;
    }

    @Override
    public boolean onClick(int x, int y) {
        MalisisGui.playSound("gui.button.press");
        fireEvent(new ClickEvent(this, x, y));
        return true;
    }

    @Override
    public boolean onButtonPress(int x, int y, MouseButton button) {
        if (button == MouseButton.LEFT) isPressed = true;
        return super.onButtonPress(x, y, button);
    }

    @Override
    public boolean onButtonRelease(int x, int y, MouseButton button) {
        if (button == MouseButton.LEFT) isPressed = false;
        return super.onButtonRelease(x, y, button);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        final GuiIcon icon;
        if (isDisabled()) icon = iconDisabled;
        else if (isPressed && isHovered()) icon = iconPressed;
        else if (isHovered()) icon = iconHovered;
        else icon = this.icon;

        rp.icon.set(icon);
        rp.colorMultiplier.set(bgColor);
        renderer.drawShape(shape, rp);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        int w = 0;
        int h = 0;
        if (image != null) {
            w = image.getWidth();
            h = image.getHeight();
        } else {
            w = (int) font.getStringWidth(text, fro);
            h = (int) font.getStringHeight(fro);
        }

        int x = (width - w) / 2;
        int y = (height - h) / 2;
        if (x == 0) x = 1;
        if (y == 0) y = 1;
        if (isPressed && isHovered()) {
            x += 1;
            y += 1;
        }

        x += offsetX;
        y += offsetY;

        if (image != null) {
            image.setPosition(x, y);
            image.setZIndex(zIndex);
            image.draw(renderer, mouseX, mouseY, partialTick);
        } else {
            renderer.drawText(font, text, x, y, 0, isHovered() ? hoveredFro : fro);
        }
    }

    @Override
    public String getPropertyString() {
        return (image != null ? "{" + image + "}" : text) + " | " + super.getPropertyString();
    }

    /**
     * Event fired when a {@link UIButton} is clicked.
     */
    public static class ClickEvent extends ComponentEvent<UIButton> {

        /** Position of the mouse when clicked . */
        private final int x, y;

        /**
         * Instantiates a new {@link ClickEvent}.
         *
         * @param component the component
         * @param x         the x coordinate of the mouse
         * @param y         the y coordinate of the mouse
         */
        public ClickEvent(UIButton component, int x, int y) {
            super(component);
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the x coordinate of the mouse.
         *
         * @return the x
         */
        public int getX() {
            return x;
        }

        /**
         * Gets the y coordinate of the mouse.
         *
         * @return the y
         */
        public int getY() {
            return y;
        }
    }
}
