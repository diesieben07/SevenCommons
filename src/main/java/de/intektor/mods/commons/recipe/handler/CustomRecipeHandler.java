package de.intektor.mods.commons.recipe.handler;

import java.util.ArrayList;

import de.intektor.mods.commons.recipe.BasicRecipe;
import net.minecraft.item.ItemStack;

/**
 * @author Intektor
 */
public class CustomRecipeHandler {

	private ArrayList<BasicRecipe> recipes = new ArrayList<BasicRecipe>();
	private ItemStack[] slots;
	
	public CustomRecipeHandler(ItemStack[] slots) {
		
	}
	
	public void update(){
		if(isThereARecipeWorking()){
			
		}
	}
	
	public boolean isThereARecipeWorking(){
		boolean flag = false;
		for(int i = 0; i < recipes.size(); i++){
			if(recipes.get(i).doesRecipeWork(slots)){
				flag = true;
			}
		}
		return flag;
	}
	
	/**
	 * returns null if there is no working recipe, and return the ID of the Working one
	 */
	public String getWorkingRecipe(){
		for(int i = 0; i < recipes.size(); i++){
			if(recipes.get(i).doesRecipeWork(slots)){
				return recipes.get(i).getID();
			}
		}
		return null;
	}
	
	public void onOutPutTaken(){
		
	}
}
