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

package net.malisis.core.renderer.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;

import lombok.Getter;

/**
 * Base class for anything drawn with a {@link MalisisRenderer}. Supports basic transformations like scaling,
 * translation and rotations.
 *
 * @author Ordinastie
 *
 */
public class Shape implements ITransformable.Translate, ITransformable.Rotate, ITransformable.Scale {

    @Getter
    protected Face[] faces;

    /** The matrix containing all the transformations applied to this {@link Shape}. */
    protected Matrix4f transformMatrix = new Matrix4f();

    /** The merged vertexes making up this {@link Shape}. */
    protected Map<String, MergedVertex> mergedVertexes;

    {
        resetMatrix();
    }

    public void reset() {
        for (Face f : this.faces) f.reset();
        this.resetMatrix();
        if (this.mergedVertexes != null) this.mergedVertexes.clear();
    }

    /**
     * Instantiates a new {@link Shape}.
     */
    public Shape() {
        this.faces = new Face[0];
    }

    /**
     * Instantiates a new {@link Shape}.
     */
    public Shape(int capacity) {
        this.faces = new Face[capacity];
    }

    /**
     * Instantiates a new {@link Shape}.
     *
     * @param faces the faces
     */
    public Shape(Face... faces) {
        this.faces = faces;
    }

    /**
     * Instantiates a new {@link Shape}.
     *
     * @param faces the faces
     */
    public Shape(List<Face> faces) {
        this(faces.toArray(new Face[0]));
    }

    /**
     * Instantiates a new {@link Shape}.
     *
     * @param s the s
     */
    public Shape(Shape s) {
        Face[] shapeFaces = s.getFaces();
        this.faces = new Face[shapeFaces.length];
        for (int i = 0; i < shapeFaces.length; i++) faces[i] = new Face(shapeFaces[i]);
        copyMatrix(s);
    }

    public Shape copy(Shape s) {
        if (this.faces.length != s.faces.length) this.faces = new Face[s.faces.length];

        for (int i = 0; i < s.faces.length; ++i) this.faces[i].copy(s.faces[i]);
        this.copyMatrix(s);
        return this;
    }

    // #region FACES
    /**
     * Adds {@link Face faces} to this {@link Shape}.
     *
     * @param faces the faces
     * @return this {@link Shape}
     */
    public Shape addFaces(Face[] faces) {
        return addFaces(faces, null);
    }

    /**
     * Adds {@link Face faces} to this {@link Shape} with the specified <b>groupName</b>.
     *
     * @param faces     the faces
     * @param groupName the group name
     * @return this {@link Shape}
     */
    public Shape addFaces(Face[] faces, String groupName) {
        if (groupName != null) {
            for (Face f : faces) f.setName(groupName);
        }

        this.faces = ArrayUtils.addAll(this.faces, faces);

        return this;
    }

    /**
     * Gets the {@link Face faces} that make up this {@link Shape} which match the specified <b>name</b>.
     *
     * @param name the name
     * @return the faces
     */
    public List<Face> getFaces(String name) {
        List<Face> list = new ArrayList<>();
        for (Face f : faces) if (f.name()
            .equalsIgnoreCase(name)) list.add(f);
        return list;
    }

    /**
     * Gets a face from its name.
     *
     * @param name the name
     * @return the face
     */
    public Face getFace(String name) {
        List<Face> list = getFaces(name);
        return !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * Removes a {@link Face} from this {@link Shape}. Has no effect if the <code>Face</code> doesn't belong to this
     * <code>Shape</code>.
     *
     * @param face the face
     * @return this {@link Shape}
     */
    public Shape removeFace(Face face) {
        if (mergedVertexes != null) {
            for (Vertex v : face.getVertexes()) {
                MergedVertex mv = getMergedVertex(v);
                mv.removeVertex(v);
            }
        }

        faces = ArrayUtils.removeElement(faces, face);
        return this;
    }

    // #end FACES

    // #region VERTEXES
    /**
     * Enables the {@link MergedVertex} for this {@link Shape}. Will transfer the current transformation matrix to the
     * {@link MergedVertex}
     */
    public void enableMergedVertexes() {
        if (mergedVertexes != null) return;

        this.mergedVertexes = MergedVertex.getMergedVertexes(this);
        // transfer current transforms into the mergedVertexes if any
        for (MergedVertex mv : mergedVertexes.values()) mv.copyMatrix(transformMatrix);
    }

    /**
     * Gets a list of {@link Vertex} with a base name containing <b>name</b>.
     *
     * @param name the name
     * @return the vertexes
     */
    public List<Vertex> getVertexes(String name) {
        List<Vertex> vertexes = new ArrayList<>();
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) {
                if (v.baseName()
                    .toLowerCase()
                    .contains(name.toLowerCase())) vertexes.add(v);
            }
        }
        return vertexes;
    }

    /**
     * Gets a list of {@link Vertex} with a base name containing {@link Face} name.
     *
     * @param face the face
     * @return the vertexes
     */
    public List<Vertex> getVertexes(Face face) {
        if (face == null) return new ArrayList<>();

        return getVertexes(face.name());
    }

    /**
     * Gets a list of {@link Vertex} with a base name containing the {@link ForgeDirection} name.
     *
     * @param direction the direction
     * @return the vertexes
     */
    public List<Vertex> getVertexes(ForgeDirection direction) {
        return getVertexes(Face.nameFromDirection(direction));
    }

    /**
     * Gets the {@link MergedVertex} for the specified {@link Vertex}.
     *
     * @param vertex the vertex
     * @return the merged vertex
     */
    public MergedVertex getMergedVertex(Vertex vertex) {
        if (mergedVertexes == null) return null;
        return mergedVertexes.get(vertex.baseName());
    }

    /**
     * Gets a list of {@link MergedVertex} with a name containing the specified <b>name</b>.
     *
     * @param names the names
     * @return the merged vertexes
     */
    public List<MergedVertex> getMergedVertexes(String... names) {
        if (mergedVertexes == null || names == null || names.length == 0) return new ArrayList<>();

        List<MergedVertex> vertexes = new ArrayList<>();
        for (MergedVertex mv : mergedVertexes.values()) {
            if (mv.is(names)) vertexes.add(mv);
        }

        return vertexes;
    }

    /**
     * Gets a list of {@link MergedVertex} with a base name containing {@link Face} name.
     *
     * @param face the face
     * @return the merged vertexes
     */
    public List<MergedVertex> getMergedVertexes(Face face) {
        if (face == null) return new ArrayList<>();

        return getMergedVertexes(face.name());
    }

    /**
     * Gets a list of {@link MergedVertex} with a base name containing the {@link ForgeDirection} name.
     *
     * @param direction the direction
     * @return the merged vertexes
     */
    public List<MergedVertex> getMergedVertexes(ForgeDirection direction) {
        return getMergedVertexes(Face.nameFromDirection(direction));
    }

    // #end VERTEXES

    protected void resetMatrix() {
        transformMatrix.identity();
        transformMatrix.translate(0.5F, 0.5F, 0.5F);
    }

    /**
     * Copies the transformation matrix from a {@link Shape shape} to this <code>Shape</code>.
     *
     * @param shape the shape
     * @return the shape
     */
    public Shape copyMatrix(Shape shape) {
        this.transformMatrix.set(shape.transformMatrix);
        return this;
    }

    /**
     * Applies the transformations matrix to this {@link Shape}. This modifies the position of the {@link Vertex
     * vertexes} making up the {@link Face faces} of this <code>Shape</code>.
     *
     * @return the shape
     */
    public Shape applyMatrix() {
        if (mergedVertexes != null) {
            for (MergedVertex mv : mergedVertexes.values()) mv.applyMatrix();

            return this;
        }

        // transform back to original place
        transformMatrix.translate(-0.5F, -0.5F, -0.5F);

        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) if (v != null) v.applyMatrix(transformMatrix);
        }

        resetMatrix();

        return this;
    }

    /**
     * Sets the parameters for all the {@link Face faces} making up this {@link Shape}.
     *
     * @param params the params
     * @param merge  the merge
     * @return this {@link Shape}
     */
    public Shape setParameters(RenderParameters params, boolean merge) {
        for (Face f : faces) if (merge) f.getParameters()
            .merge(params);
        else f.setParameters(params);

        return this;
    }

    /**
     * Set {@link RenderParameters} for {@link Face faces} matching the specified <b>name</b>. If <b>merge</b> is true,
     * the parameters will be merge with the <code>face</code> parameters instead of completely overriding them.
     *
     * @param name   the name
     * @param params the params
     * @param merge  the merge
     * @return this {@link Shape}
     */
    public Shape setParameters(String name, RenderParameters params, boolean merge) {
        for (Face f : this.faces) {
            if (!f.name.equalsIgnoreCase(name)) continue;

            if (merge) f.getParameters()
                .merge(params);
            else f.setParameters(params);
        }
        return this;
    }

    /**
     * Sets the size of this {@link Shape}. <b>width</b> represents East-West axis, <b>height</b> represents Bottom-Top
     * axis and <b>Depth</b> represents North-South axis. The calculations are based on {@link Vertex#baseName()}.
     *
     * @param width  the width
     * @param height the height
     * @param depth  the depth
     * @return this {@link Shape}
     */
    public Shape setSize(float width, float height, float depth) {
        float x = 0, y = 0, z = 0;
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) {
                final int flags = v.getDirectionFlags();
                if ((flags & Vertex.WEST) != 0) x = (float) v.getX();
                if ((flags & Vertex.DOWN) != 0) y = (float) v.getY();
                if ((flags & Vertex.NORTH) != 0) z = (float) v.getZ();
            }
        }

        return setBounds(x, y, z, x + width, y + height, z + depth);
    }

    /**
     * Sets the bounds for this {@link Shape}. Calculations are based on {@link Vertex#baseName()}.
     *
     * @param aabb the aabb
     * @return this {@link Shape}
     */
    public Shape setBounds(AxisAlignedBB aabb) {
        if (aabb == null) return this;
        return setBounds(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    /**
     * Sets the bounds for this {@link Shape}. Calculations are based on {@link Vertex#baseName()}.
     *
     * @param minX the x
     * @param minY the y
     * @param minZ the z
     * @param maxX the x
     * @param maxY the y
     * @param maxZ the z
     * @return this {@link Shape}
     */
    public Shape setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) {
                final int flags = v.getDirectionFlags();
                if ((flags & Vertex.WEST) != 0) v.setX(minX);
                if ((flags & Vertex.EAST) != 0) v.setX(maxX);
                if ((flags & Vertex.DOWN) != 0) v.setY(minY);
                if ((flags & Vertex.UP) != 0) v.setY(maxY);
                if ((flags & Vertex.NORTH) != 0) v.setZ(minZ);
                if ((flags & Vertex.SOUTH) != 0) v.setZ(maxZ);
            }
        }
        return this;
    }

    /**
     * Limits this {@link Shape} to the bounding box passed.
     *
     * @param aabb the aabb
     * @return this {@link Shape}
     */
    public Shape limit(AxisAlignedBB aabb) {
        return limit(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    /**
     * Limits this {@link Shape} to the bounding box passed.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param X the x
     * @param Y the y
     * @param Z the z
     * @return the shape
     */
    public Shape limit(double x, double y, double z, double X, double Y, double Z) {
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) {
                v.setX(Vertex.clamp(v.getX(), x, X));
                v.setY(Vertex.clamp(v.getY(), y, Y));
                v.setZ(Vertex.clamp(v.getZ(), z, Z));
            }
        }
        return this;
    }

    /**
     * Translates this {@link Shape}.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    @Override
    public void translate(float x, float y, float z) {
        if (mergedVertexes != null) {
            for (MergedVertex mv : mergedVertexes.values()) mv.translate(x, y, z);
        } else transformMatrix.translate(x, y, z);
    }

    /**
     * Scales this {@link Shape} on all axis.
     *
     * @param factor the factor
     */
    public void scale(float factor) {
        scale(factor, factor, factor, 0, 0, 0);
    }

    /**
     * Scale.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void scale(float x, float y, float z) {
        scale(x, y, z, 0, 0, 0);
    }

    /**
     * Scales this {@link Shape}.
     *
     * @param x       the x
     * @param y       the y
     * @param z       the z
     * @param offsetX the offset x
     * @param offsetY the offset y
     * @param offsetZ the offset z
     */
    @Override
    public void scale(float x, float y, float z, float offsetX, float offsetY, float offsetZ) {
        if (mergedVertexes != null) {
            for (MergedVertex mv : mergedVertexes.values()) mv.scale(x, y, z, offsetX, offsetY, offsetZ);
        } else {
            translate(offsetX, offsetY, offsetZ);
            transformMatrix.scale(x, y, z);
            translate(-offsetX, -offsetY, -offsetZ);
        }
    }

    /**
     * Rotates this {@link Shape} around the given axis the specified angle.
     *
     * @param angle the angle
     * @param x     the x
     * @param y     the y
     * @param z     the z
     */
    public void rotate(float angle, float x, float y, float z) {
        rotate(angle, x, y, z, 0, 0, 0);
    }

    /**
     * Rotates this {@link Shape} around the given axis the specified angle. Offsets the origin for the rotation.
     *
     * @param angle   the angle
     * @param x       the x
     * @param y       the y
     * @param z       the z
     * @param offsetX the offset x
     * @param offsetY the offset y
     * @param offsetZ the offset z
     */
    @Override
    public void rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ) {
        if (mergedVertexes != null) {
            for (MergedVertex mv : mergedVertexes.values()) mv.rotate(angle, x, y, z, offsetX, offsetY, offsetZ);
        } else {
            translate(offsetX, offsetY, offsetZ);
            transformMatrix.rotate((float) Math.toRadians(angle), x, y, z);
            translate(-offsetX, -offsetY, -offsetZ);
        }
    }

    /**
     * Stores the current state of each {@link Vertex} making up this {@link Shape}.
     *
     * @return this {@link Shape}
     */
    public Shape storeState() {
        applyMatrix();
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) if (v != null) v.setInitialState();
        }
        return this;
    }

    /**
     * Resets the state of each {@link Vertex} making up this {@link Shape} to a previously stored one.
     *
     * @return this {@link Shape}
     */
    public Shape resetState() {
        resetMatrix();
        for (Face f : faces) {
            for (Vertex v : f.getVertexes()) if (v != null) v.resetState();
        }
        return this;
    }

    /**
     * Interpolates the UVs of each vertex making up this {@link Shape} based on their position and the {@link Face}
     * orientation.
     *
     * @return this {@link Shape}
     */
    public Shape interpolateUV() {
        for (Face f : faces) f.interpolateUV();

        return this;
    }

    /**
     * Shrinks the {@link Face} matching <b>face</b> name by a certain <b>factor</b>. The {@link Vertex vertexes} of
     * connected faces are moved too.
     *
     * @param dir    the dir
     * @param factor the factor
     * @return the shape
     */
    public Shape shrink(ForgeDirection dir, float factor) {
        Face face = getFace(Face.nameFromDirection(dir));
        if (face == null) return this;
        enableMergedVertexes();

        double x = 0, y = 0, z = 0;
        for (Vertex v : face.getVertexes()) {
            x += v.getX() / 4;
            y += v.getY() / 4;
            z += v.getZ() / 4;
        }
        face.scale(factor, x, y, z);

        for (Vertex v : face.getVertexes()) {
            for (Vertex sv : mergedVertexes.get(v.baseName())) {
                if (sv != v) sv.set(v.getX(), v.getY(), v.getZ());
            }
        }

        return this;
    }

    public void deductParameters() {
        for (Face f : faces) f.deductParameters();
    }

    /**
     * Builds a {@link Shape} from multiple ones.
     *
     * @param shapes the shapes
     * @return the shape
     */
    public static Shape fromShapes(Shape... shapes) {
        Face[] faces = new Face[0];
        for (Shape s : shapes) {
            s.applyMatrix();
            faces = ArrayUtils.addAll(faces, s.getFaces());
        }

        return new Shape(faces);
    }

    /**
     * Builds a {@link Shape} from multiple ones. This is a shallow copy!
     *
     * @param shapes the shapes
     * @return self
     */
    public Shape takeShapes(Shape... shapes) {

        int size = 0;
        for (int i = 0; i < shapes.length; ++i) size += shapes[i].faces.length;

        if (this.faces.length != size) this.faces = new Face[size];

        size = 0;
        for (int i = 0; i < shapes.length; ++i) {
            shapes[i].applyMatrix();
            System.arraycopy(shapes[i].faces, 0, this.faces, size, shapes[i].faces.length);
            size += shapes[i].faces.length;
        }

        return this;
    }

    /**
     * See {@link #addFaces(Face[], String)}. This takes the faces from shapes. This is a shallow copy!
     *
     * @param shapes the shapes
     * @return self
     */
    public Shape takeFaces(String[] groupNames, Shape... shapes) {

        int size = 0;
        for (int i = 0; i < shapes.length; ++i) {
            size += shapes[i].faces.length;
            for (Face f : shapes[i].faces) f.setName(groupNames[i]);
        }

        if (this.faces.length != size) this.faces = new Face[size];

        size = 0;
        for (int i = 0; i < shapes.length; ++i) {
            System.arraycopy(shapes[i].faces, 0, this.faces, size, shapes[i].faces.length);
            size += shapes[i].faces.length;
        }

        return this;
    }
}
