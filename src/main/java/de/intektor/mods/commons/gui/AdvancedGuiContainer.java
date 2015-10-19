package de.intektor.mods.commons.gui;

import java.util.ArrayList;


import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * 
 * This allows the the gui to have different states, what will cause the gui to only render 
 * certain things like buttons etc when the state is active, what can be used to make eg. different pages in a book.
 * Additional to the AdvancedGuiScreen the AdvancedGuiContainer can handle slots, and it will disable slots when they aren't enabled in the current gui state
 * @author Intektor
 */
public abstract class AdvancedGuiContainer extends GuiContainer{

	public ArrayList <GuiStateContainer> guiStates = new ArrayList<GuiStateContainer>();

	private int activeGuiState = 0;

	public TileEntity tileEntity;

	public EntityPlayer theUser;

	public AdvancedGuiContainer(Container container, EntityPlayer player) {
		super(container);
		theUser = player;
	}

	public void setGuiState(int state){
		activeGuiState = state;
		guiStates.get(state).initState();
	}

	public int getActiveGuiState(){
		return activeGuiState;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		buttonList = guiStates.get(getActiveGuiState()).buttonList;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
		if (mouseButton == 0)
		{
			ArrayList <AdvancedGuiButton> buttonList = guiStates.get(getActiveGuiState()).buttonList;
			for (int l = 0; l < buttonList.size(); ++l)
			{
				AdvancedGuiButton button = (AdvancedGuiButton) buttonList.get(l);
				if(!button.atMoon){
					if(mouseX > button.xPosition && mouseX < button.xPosition + button.width){
						if(mouseY > button.yPosition && mouseY < button.yPosition + button.height){
							if(button.enabled){
								button.mousePressed(this.mc, mouseX, mouseY);
								ActionPerformedEvent.Pre event = new ActionPerformedEvent.Pre(this, button, this.buttonList);
								if (MinecraftForge.EVENT_BUS.post(event))
									break;
								onButtonPressed(getActiveGuiState(), (AdvancedGuiButton) event.button);
								if (this.equals(this.mc.currentScreen))
									MinecraftForge.EVENT_BUS.post(new ActionPerformedEvent.Post(this, event.button, this.buttonList));
							}
						}
					}
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public abstract void onButtonPressed(int StateID, AdvancedGuiButton button);
}
