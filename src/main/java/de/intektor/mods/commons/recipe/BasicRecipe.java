package de.intektor.mods.commons.recipe;

import net.minecraft.item.ItemStack;

public abstract class BasicRecipe {

	private final String ID;
	
	protected BasicRecipe(String id){
		ID = id;
	}
	
	public abstract boolean doesRecipeWork(ItemStack[] slots);
	public abstract ItemStack[] createOutputStacks(ItemStack[] slots);
	
	public String getID(){
		return ID;
	}
	
	public int getTheOutputSlot
}
