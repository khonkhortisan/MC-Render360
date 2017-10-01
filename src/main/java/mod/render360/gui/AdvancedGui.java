package mod.render360.gui;

import java.util.List;

import mod.render360.RenderUtil;
import mod.render360.gui.advanced.Advanced;
import mod.render360.gui.advanced.CubicGui;
import mod.render360.gui.advanced.EquirectangularGui;
import mod.render360.gui.advanced.HammerGui;
import mod.render360.gui.advanced.FisheyeGui;
import mod.render360.render.Hammer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AdvancedGui implements Settings {
	
	private static Advanced guiObject = new EquirectangularGui();
	
	public AdvancedGui() {
		RenderUtil.renderMethod = guiObject.getRenderMethod();
		RenderUtil.forceReload();
	}
	
	@Override
	public void initGui(List<GuiButton> buttonList, int width, int height) {
		GuiButton button = new GuiButton(18130, width / 2 - 190 + ((190*2-10*3)/4 + 10) * 0, height / 6 + 24, (190*2-10*3)/4, 20, "Cubic");
		if (guiObject instanceof CubicGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18131, width / 2 - 190 + ((190*2-10*3)/4 + 10) * 1, height / 6 + 24, (190*2-10*3)/4, 20, "Hammer");
		if (guiObject instanceof HammerGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18132, width / 2 - 190 + ((190*2-10*3)/4 + 10) * 2, height / 6 + 24, (190*2-10*3)/4, 20, "Equirectangular");
		if (guiObject instanceof EquirectangularGui) button.enabled = false;
		buttonList.add(button);
		
		button = new GuiButton(18133, width / 2 - 190 + ((190*2-10*3)/4 + 10) * 3, height / 6 + 24, (190*2-10*3)/4, 20, "Fisheye");
		if (guiObject instanceof FisheyeGui) button.enabled = false;
		buttonList.add(button);
		
		guiObject.initGui(buttonList, width, height);
	}
	
	@Override
	public void actionPerformed(GuiButton guiButton, GuiScreen parentScreen) {
		switch(guiButton.id) {
		case 18130: //Cubic
			guiObject = new CubicGui();
			RenderUtil.renderMethod = guiObject.getRenderMethod();
			RenderUtil.forceReload();
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
			break;
		case 18131: //Hammer
			guiObject = new HammerGui();
			RenderUtil.renderMethod = guiObject.getRenderMethod();
			RenderUtil.forceReload();
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
			break;
		case 18132: //Equirectangular
			guiObject = new EquirectangularGui();
			RenderUtil.renderMethod = guiObject.getRenderMethod();
			RenderUtil.forceReload();
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
			break;
		case 18133: //Fisheye
			guiObject = new FisheyeGui();
			RenderUtil.renderMethod = guiObject.getRenderMethod();
			RenderUtil.forceReload();
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(parentScreen));
			break;
		default:
			guiObject.actionPerformed(guiButton);
			break;
		}
	}
}
