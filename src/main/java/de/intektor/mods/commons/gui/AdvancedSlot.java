package de.intektor.mods.commons.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * 
 * @author Intektor
 *
 */

public class AdvancedSlot extends Slot{

	private int xNormal;
	private int yNormal;
	public AdvancedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		xNormal = xPosition;
		yNormal = yPosition;
	}

	public int getNormalX(){
		return xNormal;
	}
	
	public int getNormalY(){
		return yNormal;
	}
	
	public void setNormalX(int x){
		xNormal = x;
	}
	
	public void setNormalY(int y){
		yNormal = y;
	}
	
	public void shootToMoon(){
		this.xDisplayPosition = -100000;
		this.yDisplayPosition = -100000;
	}
	
	public void setNormal(){
		this.xDisplayPosition = xNormal;
		this.yDisplayPosition = yNormal;
	}
}
