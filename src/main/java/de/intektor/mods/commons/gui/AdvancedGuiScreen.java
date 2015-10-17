package de.intektor.mods.commons.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * This allows the the gui to have different states, what will cause the gui to only render 
 * certain things like buttons etc when the state is active, what can be used to make eg. different pages in a book
 * @author Intektor
 */
public abstract class AdvancedGuiScreen extends GuiScreen{
	
	public ArrayList <GuiState> guiStates = new ArrayList<GuiState>();

	private int activeGuiState = 0;

	public EntityPlayer theUser;

	public AdvancedGuiScreen(EntityPlayer player) {
		theUser = player;
	}

	public void setGuiState(int state){
		activeGuiState = state;
	}

	public int getActiveGuiState(){
		return activeGuiState;
	}

	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		buttonList = guiStates.get(getActiveGuiState()).buttonList;
		super.drawScreen(mouseX, mouseY, partialTicks);
		for(int i = 0; i < guiStates.get(getActiveGuiState()).buttonList.size(); i++){
			AdvancedGuiButton button = guiStates.get(getActiveGuiState()).buttonList.get(i);
			
			if(mouseX > button.xPosition && mouseX < button.xPosition + button.width){
				if(mouseY > button.yPosition && mouseY < button.yPosition + button.height){
					onHoveredOverButton(getActiveGuiState(), button, mouseX, mouseY);
				}
			}
		}
	}
	@Override
	public void initGui() {
		super.initGui();
	}
	
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
	}
	
	public abstract void onButtonPressed(int StateID, AdvancedGuiButton button);

	public abstract void onHoveredOverButton(int StateID, AdvancedGuiButton button, int mouseX, int mouseY);
}
