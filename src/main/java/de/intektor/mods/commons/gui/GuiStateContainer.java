package de.intektor.mods.commons.gui;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

/**
 * @author Intektor
 */

public class GuiStateContainer extends GuiState{

	private Container container;
	public int[] slotIDs;
	
	public GuiStateContainer(Container container, ResourceLocation texture, int[] slotsID, AdvancedGuiButton[] buttonIDs) {
		super(texture, buttonIDs);
		this.container = container;
		this.slotIDs = slotsID;
	}

	public void initState(){
		for(int x = 0; x < container.inventorySlots.size(); x++){
			if(container.inventorySlots.get(x) instanceof AdvancedSlot){
				AdvancedSlot slot = (AdvancedSlot) container.inventorySlots.get(x);
				if(!isIDpartSlots(x)){
					slot.shootToMoon();
				}else{
					slot.setNormal();
				}
			}else if(container.inventorySlots.get(x) instanceof AdvancedResultSlot){
				AdvancedResultSlot slot = (AdvancedResultSlot) container.inventorySlots.get(x);
				if(!isIDpartSlots(x)){
					slot.shootToMoon();
				}else{
					slot.setNormal();
				}
			}
		}
	}
	protected boolean isIDpartSlots(int id){
		boolean b = false;
		for(int j = 0; j < slotIDs.length; j++){
			if(slotIDs[j] == id){
				b = true;
			}
		}
		return b;
	}
}
