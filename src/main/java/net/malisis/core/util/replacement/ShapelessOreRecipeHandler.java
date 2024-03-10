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

package net.malisis.core.util.replacement;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * @author Ordinastie
 *
 */
public class ShapelessOreRecipeHandler extends ReplacementHandler<ShapelessOreRecipe> {

    // This field needs to use reflection because ATs don't really work on forge code.
    // The roughly equivalent field for the vanilla MC classes is just using ATs.
    private static final MethodHandle outputField;

    static {
        try {
            Field field = ShapelessOreRecipe.class.getDeclaredField("output");
            field.setAccessible(true);
            outputField = MethodHandles.lookup()
                .unreflectSetter(field);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access ShapelessOreRecipe.output", e);
        }
    }

    public ShapelessOreRecipeHandler() {
        super(ShapelessOreRecipe.class);
    }

    @Override
    public boolean replace(ShapelessOreRecipe recipe, Object vanilla, Object replacement) {
        boolean replaced = false;
        try {
            if (isMatched(recipe.getRecipeOutput(), vanilla)) {
                outputField.invokeExact(recipe, getItemStack(replacement));
                replaced = true;
            }

            ArrayList<Object> input = recipe.getInput();
            for (int i = 0; i < input.size(); i++) {
                if (input.get(i) instanceof ItemStack && isMatched((ItemStack) input.get(i), vanilla)) {
                    input.set(i, getItemStack(replacement));
                    replaced = true;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to replace ShapelessOreRecipe", e);
        }

        return replaced;
    }
}
