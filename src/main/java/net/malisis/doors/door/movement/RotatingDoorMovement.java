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

package net.malisis.doors.door.movement;

import static net.malisis.doors.door.block.Door.*;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.AABBUtils;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class RotatingDoorMovement implements IDoorMovement {

    @Override
    public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type) {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, DOOR_WIDTH);
        if (type == BoundingBoxType.SELECTION) {
            if (!topBlock) aabb.maxY++;
            else aabb.minY--;
        }

        if (tileEntity.isOpened()) AABBUtils.rotate(aabb, tileEntity.isReversed() ? -1 : 1);

        return aabb;
    }

    private Transformation getTransformation(DoorTileEntity tileEntity) {
        float angle = 90;
        float hingeX = 0.5F - DOOR_WIDTH / 2;
        float hingeZ = -0.5F + DOOR_WIDTH / 2;

        if (tileEntity.isReversed()) {
            hingeX = -hingeX;
            angle = -90;
        }

        Rotation rotation = new Rotation(angle);
        rotation.aroundAxis(0, 1, 0)
            .offset(hingeX, 0, hingeZ);
        rotation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
        rotation.forTicks(
            tileEntity.getDescriptor()
                .getOpeningTime());

        return rotation;
    }

    @Override
    public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp) {
        return new Animation[] { new Animation(model, getTransformation(tileEntity)) };
    }

    @Override
    public boolean isSpecial() {
        return false;
    }
}
