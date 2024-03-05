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

package net.malisis.core.renderer.element.shape;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.BottomFace;
import net.malisis.core.renderer.element.face.EastFace;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.core.renderer.element.face.SouthFace;
import net.malisis.core.renderer.element.face.TopFace;
import net.malisis.core.renderer.element.face.WestFace;
import org.lwjgl.Sys;

/**
 * Basic Cube {@link Shape} using predefined {@link Face faces}.
 *
 * @author Ordinastie
 *
 */
public class Cube extends Shape {

    public Cube() {
        super(new NorthFace(), new SouthFace(), new EastFace(), new WestFace(), new TopFace(), new BottomFace());
        storeState();
    }

    public Cube(Cube c) {
        super(c);
    }

    public Cube copy(Cube c) {
        for (int i = 0; i < 6; ++i) {
            this.faces[i].copy(c.faces[i]);
        }
        this.copyMatrix(c);
        return this;
    }

    public void reset() {
        for (Face f : this.faces) f.reset();
        this.resetMatrix();
        if (this.mergedVertexes != null)
            this.mergedVertexes.clear();
        this.storeState();
    }
}
