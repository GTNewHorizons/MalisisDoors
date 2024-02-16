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

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

/**
 * @author Ordinastie
 *
 */
public class ShapedRecipesHandler extends ReplacementHandler<ShapedRecipes> {

    public ShapedRecipesHandler() {
        super(ShapedRecipes.class);
    }

    @Override
    public boolean replace(ShapedRecipes recipe, Object vanilla, Object replacement) {
        boolean replaced = false;

        if (isMatched(recipe.getRecipeOutput(), vanilla)) {
            recipe.recipeOutput = getItemStack(replacement);
            replaced = true;
        }

        ItemStack[] input = recipe.recipeItems;
        for (int i = 0; i < input.length; i++) {
            if (input[i] instanceof ItemStack && isMatched(input[i], vanilla)) {
                input[i] = getItemStack(replacement);
                replaced = true;
            }
        }

        return replaced;
    }
}
