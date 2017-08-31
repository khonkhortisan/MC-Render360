package core.render360;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class Render360Event extends Event {

	public static class DimensionLoadEvent extends Render360Event {
		
	}
	
	public static class DimensionUnloadEvent extends Render360Event {
		
	}
	
	@Cancelable
	public static class LoadingProgressBackgroundEvent extends Render360Event {
		
		public final GuiScreen guiScreen;
		public final Framebuffer framebuffer;
		
		public LoadingProgressBackgroundEvent(GuiScreen guiScreen, Framebuffer framebuffer) {
			this.guiScreen = guiScreen;
			this.framebuffer = framebuffer;
		}
	}
	
	@Cancelable
	public static class DrawWorldBackgroundEvent extends Render360Event {
		
		public final GuiScreen guiScreen;
		
		public DrawWorldBackgroundEvent(GuiScreen guiScreen) {
			this.guiScreen = guiScreen;
		}
	}
	
	@Cancelable
	public static class DrawBackgroundEvent extends Render360Event {
		
		public final GuiScreen guiScreen;
		
		public DrawBackgroundEvent(GuiScreen guiScreen) {
			this.guiScreen = guiScreen;
		}
	}
	
	public static class RotateCameraEvent extends Render360Event {
		
		public float yaw;
		public float pitch;
		public float roll;
		
		public RotateCameraEvent() {
			this.yaw = 0;
			this.pitch = 0;
			this.roll = 0;
		}
	}
	
	public static class RotateParticleEvent extends Render360Event {
		
		public float rotationX;
		public float rotationZ;
		public float rotationYZ;
		public float rotationXY;
		public float rotationXZ;
		public float posX;
		public float posY;
		public float posZ;
		
		public RotateParticleEvent(float rotationX, float rotationZ,
				float rotationYZ, float rotationXY, float rotationXZ,
				float posX, float posY, float posZ) {
			this.rotationX = rotationX;
			this.rotationZ = rotationZ;
			this.rotationYZ = rotationYZ;
			this.rotationXY = rotationXY;
			this.rotationXZ = rotationXZ;
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
		}
	}
}
