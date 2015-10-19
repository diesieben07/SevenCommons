package de.intektor.mods.commons.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotFurnace;

/**
 * 
 * @author Intektor
 *
 */

public class AdvancedResultSlot extends SlotFurnace{

	private int xNormal;
	private int yNormal;
	
	public AdvancedResultSlot(EntityPlayer player, IInventory p_i45793_2_, int slotIndex, int xPosition, int yPosition) {
		super(player, p_i45793_2_, slotIndex, xPosition, yPosition);
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