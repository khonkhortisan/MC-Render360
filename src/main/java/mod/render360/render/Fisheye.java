package mod.render360.render;

import java.util.List;

import org.lwjgl.opengl.GL20;

import mod.render360.gui.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.EntityRenderer;

public class Fisheye extends RenderMethod {

	private final String fragmentShader = "#version 130//\n\n#define M_PI 3.14159265//\n\n/* This comes interpolated from the vertex shader */\nin vec2 texcoord;\n/* The 6 textures to be rendered */\nuniform sampler2D texFront;\nuniform sampler2D texBack;\nuniform sampler2D texLeft;\nuniform sampler2D texRight;\nuniform sampler2D texTop;\nuniform sampler2D texBottom;\nuniform int antialiasing;\nuniform vec2 pixelOffset[16];\nuniform float fovx;\nuniform float aspectratio;\nuniform bool fullframe;\nuniform int fisheyetype;\nuniform vec4 backgroundColor;\nuniform vec2 cursorPos;\nuniform bool drawCursor;\nout vec4 color;\nvec3 rotate(vec3 ray, vec2 angle) {\n  //rotate y\n\n  float y = -sin(angle.y)*ray.z;\n  float z = cos(angle.y)*ray.z;\n  ray.y = y;\n  ray.z = z;\n  //rotate x\n\n  float x = -sin(angle.x)*ray.z;\n  z = cos(angle.x)*ray.z;\n  ray.x = x;\n  ray.z = z;\n  return ray;\n}\nvoid main(void) {\n  /* Ray-trace a cube */\n	//Anti-aliasing\n	vec4 colorN[16];\n	for (int loop = 0; loop < antialiasing; loop++) {\n		//create ray\n\n		vec3 ray = vec3(0, 0, -1);\n		//fisheye stuff\n		\n		//point relative to center [0..1] -> [-1..1]\n		float cx = (texcoord.x+pixelOffset[loop].x)*2-1;\n		float cy = (texcoord.y+pixelOffset[loop].y)*2-1;\n		\n		//scale from square view to window shape view //fcontain\n		if (aspectratio > 1) {\n			cx = cx * aspectratio;\n		} else {\n			cy = cy / aspectratio;\n		}\n		\n		if (fullframe) {\n			//scale circle radius [1] up to screen diagonal radius [sqrt(2) or higher]\n			if (aspectratio > 1) {\n				cx = cx / sqrt(aspectratio*aspectratio+1*1);\n				cy = cy / sqrt(aspectratio*aspectratio+1*1);\n			} else {\n				cx = cx / sqrt((1/aspectratio)*(1/aspectratio)+1*1);\n				cy = cy / sqrt((1/aspectratio)*(1/aspectratio)+1*1);\n			}\n		} else {\n			//only draw center circle\n			if (cx*cx+cy*cy > 1) {\n				color = backgroundColor;\n				return;\n			}\n		}\n		\n		//decrease fov by slider\n		float limitedfov = fovx;\n		//and by projection limit\n		if (fisheyetype == 2) {//orthographic [-1..1] -> [-0.5..0.5]\n			limitedfov = min(limitedfov, 180);\n		}\n		cx = cx * limitedfov/360;\n		cy = cy * limitedfov/360;\n		\n		float r;\n		float theta;\n		if (fisheyetype == 0) {//equidistant\n			//This is the x scale of the theta= equation. Not related to fov.\n			//it's the result of the forward equation with theta=pi\n			//forward: r=f*theta\n			float maxr = M_PI;\n				//scale to angle (equidistant) [-1..1] -> [-pi..pi] (orthographic [-0.5..0.5] -> [-pi/2..pi/2]\n				cx = cx * maxr;\n				cy = cy * maxr;\n				//angle from forward <=abs(pi) or <=abs(pi/2)\n				r = sqrt(cx*cx+cy*cy);\n			//reverse:\n			theta = r;\n		} else if (fisheyetype == 1) {//stereographic\n			//forward: r=2f*tan(theta/2)\n			float maxr = 2*tan(M_PI*0.5);\n				cx = cx * maxr;\n				cy = cy * maxr;\n				r = sqrt(cx*cx+cy*cy);\n			//reverse:\n			theta = 2*atan(r*0.5);\n		} else if (fisheyetype == 2) {//orthographic\n			//forward: r=f*sin(theta)\n			float maxr = sin(M_PI);\n				cx = cx * maxr;\n				cy = cy * maxr;\n				r = sqrt(cx*cx+cy*cy);\n			//reverse:\n			theta = asin(r);\n		} else if (fisheyetype == 3) {//equisolid\n			//forward: r=2f*sin(theta/2)\n			float maxr = 2*sin(M_PI*0.5);\n				cx = cx * maxr;\n				cy = cy * maxr;\n				r = sqrt(cx*cx+cy*cy);\n			//reverse:\n			theta = 2*asin(r*0.5);\n		} else if (fisheyetype == 4) {//thoby\n			//forward: r=1.47*f*sin(0.713*theta)\n			float maxr = 1.47*sin(0.713*M_PI);\n				cx = cx * maxr;\n				cy = cy * maxr;\n				r = sqrt(cx*cx+cy*cy);\n			//reverse:\n			theta = asin(r/1.47)/0.713;\n		}\n		//rotate ray\n		float s = sin(theta);\n		float x = (cx)/r*s, y = (cy)/r*s, z = cos(theta);\n		\n		//other-handed coordinate system\n		ray.x = x; ray.y = y; ray.z = -z;\n		//find which side to use\n\n		if (abs(ray.x) > abs(ray.y)) {\n			if (abs(ray.x) > abs(ray.z)) {\n				if (ray.x > 0) {\n					//right\n\n					float x = ray.z / ray.x;\n					float y = ray.y / ray.x;\n					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//left\n\n					float x = -ray.z / -ray.x;\n					float y = ray.y / -ray.x;\n					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		} else {\n			if (abs(ray.y) > abs(ray.z)) {\n				if (ray.y > 0) {\n					//top\n\n					float x = ray.x / ray.y;\n					float y = ray.z / ray.y;\n					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//bottom\n\n					float x = ray.x / -ray.y;\n					float y = -ray.z / -ray.y;\n					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			} else {\n				if (ray.z > 0) {\n					//back\n\n					float x = -ray.x / ray.z;\n					float y = ray.y / ray.z;\n					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				} else {\n					//front\n\n					float x = ray.x / -ray.z;\n					float y = ray.y / -ray.z;\n					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);\n				}\n			}\n		}\n		if (drawCursor) {\n			vec2 normalAngle = cursorPos*2 - 1;\n			float x = ray.x / -ray.z;\n			float y = ray.y / -ray.z;\n			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&\n				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&\n				ray.z < 0) {\n				colorN[loop] = vec4(1, 1, 1, 1);\n			}\n		}\n	}\n	if (antialiasing == 16) {\n	  vec4 corner[4];\n	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);\n	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);\n	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);\n	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);\n	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);\n	}\n	else if (antialiasing == 4) {\n		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);\n	}\n	else { //if antialiasing == 1\n		color = colorN[0];\n	}\n}\n";

	private boolean skyBackground = false;

	private float fov = 360;

	private int fisheyetype = 0;
	
	private boolean fullframe = false;

	@Override
	public String getName() {
		return "Fisheye";
	}

	@Override
	public String getFragmentShader() {
		return this.fragmentShader;
	}

	@Override
	public boolean replaceLoadingScreen() {
		return true;
	}

	@Override
	public void addButtonsToGui(List<GuiButton> buttonList, int width, int height) {
		super.addButtonsToGui(buttonList, width, height);
		buttonList.add(new Slider(new Responder(), 18104, width / 2 - 180, height / 6 + 24, 360, 20, "FOV", 0f, 360f, fov, 1f, null));
		buttonList.add(new GuiButton(18103, width / 2 - 155, height / 6 + 72, 150, 20, "Background Color: " + (skyBackground ? "Sky" : "Black")));
		buttonList.add(new GuiButton(18110, width / 2 + 100, height / 6 - 12, (width - 10) - (width / 2 + 100), 20, (fisheyetype==0?"Equidistant":"")+(fisheyetype==1?"Stereographic":"")+(fisheyetype==2?"Orthographic":"")+(fisheyetype==3?"Equisolid":"")+(fisheyetype==4?"Thoby":"")));
		buttonList.add(new GuiButton(18111, width / 2 + 5, height / 6 + 120, 150, 20, "Full Frame: " + (fullframe ? "ON" : "OFF")));
	}

	@Override
	public void onButtonPress(GuiButton button) {
		super.onButtonPress(button);
		//Background Color
		if (button.id == 18103) {
			skyBackground = !skyBackground;
			button.displayString = "Background Color: " + (skyBackground ? "Sky" : "Black");
		}
		if (button.id == 18110) {
			fisheyetype+=1;
			if (fisheyetype>4)
				fisheyetype=0;
			button.displayString = (fisheyetype==0?"Equidistant":"")+(fisheyetype==1?"Stereographic":"")+(fisheyetype==2?"Orthographic":"")+(fisheyetype==3?"Equisolid":"")+(fisheyetype==4?"Thoby":"");
		}
		if (button.id == 18111) {
			fullframe = !fullframe;
			button.displayString = "Full Frame: " + (fullframe ? "ON" : "OFF");
		}
	}
	
	@Override
	public float[] getBackgroundColor() {
		if (skyBackground) {
			EntityRenderer er = Minecraft.getMinecraft().entityRenderer;
			return new float[] {er.fogColorRed, er.fogColorGreen, er.fogColorBlue};
		} else {
			return null;
		}
	}

	@Override
	public float getFOV() {
		return fov;
	}

	@Override
	public int getfisheyetype() {
		return fisheyetype;
	}
	
	@Override
	public boolean getfullframe() {
		return fullframe;
	}

	public class Responder implements GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {

		}

		@Override
		public void setEntryValue(int id, float value) {
			//FOV
			if (id == 18104) {
				fov = value;
			}
		}

		@Override
		public void setEntryValue(int id, String value) {

		}
	}
}
