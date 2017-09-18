#version 130//\n

#define M_PI 3.14159265//\n

/* This comes interpolated from the vertex shader */
in vec2 texcoord;

/* The 6 textures to be rendered */
uniform sampler2D texFront;
uniform sampler2D texBack;
uniform sampler2D texLeft;
uniform sampler2D texRight;
uniform sampler2D texTop;
uniform sampler2D texBottom;

uniform int antialiasing;

uniform vec2 pixelOffset[16];

uniform float fovx;

uniform float aspectratio;

uniform bool fullframe;

uniform int fisheyetype;

uniform vec4 backgroundColor;

uniform vec2 cursorPos;

uniform bool drawCursor;

out vec4 color;

vec3 rotate(vec3 ray, vec2 angle) {

  //rotate y\n
  float y = -sin(angle.y)*ray.z;
  float z = cos(angle.y)*ray.z;
  ray.y = y;
  ray.z = z;

  //rotate x\n
  float x = -sin(angle.x)*ray.z;
  z = cos(angle.x)*ray.z;
  ray.x = x;
  ray.z = z;

  return ray;
}

void main(void) {
  /* Ray-trace a cube */

	//Anti-aliasing
	vec4 colorN[16];

	for (int loop = 0; loop < antialiasing; loop++) {

		//create ray\n
		vec3 ray = vec3(0, 0, -1);

		//fisheye stuff
		
		//point relative to center [0..1] -> [-1..1]
		float cx = (texcoord.x+pixelOffset[loop].x)*2-1;
		float cy = (texcoord.y+pixelOffset[loop].y)*2-1;
		
		//scale from square view to window shape view //fcontain
		if (aspectratio > 1) {
			cx = cx * aspectratio;
		} else {
			cy = cy / aspectratio;
		}
		
		if (fullframe) {
			//scale circle radius [1] up to screen diagonal radius [sqrt(2) or higher]
			if (aspectratio > 1) {
				cx = cx / sqrt(aspectratio*aspectratio+1*1);
				cy = cy / sqrt(aspectratio*aspectratio+1*1);
			} else {
				cx = cx / sqrt((1/aspectratio)*(1/aspectratio)+1*1);
				cy = cy / sqrt((1/aspectratio)*(1/aspectratio)+1*1);
			}
		} else {
			//only draw center circle
			if (cx*cx+cy*cy > 1) {
				color = backgroundColor;
				return;
			}
		}
		
		//max theta as limited by fov
		float fovtheta = fovx*M_PI/360;
		float r;
		float theta;
		if (fisheyetype == 0) {//equidistant
			//This is the x scale of the theta= equation. Not related to fov.
			//it's the result of the forward equation with theta=pi
			//forward: r=f*theta
			float maxr = fovtheta;
				//scale to angle (equidistant) [-1..1] -> [-pi..pi] (orthographic [-0.5..0.5] -> [-pi/2..pi/2]
				cx = cx * maxr;
				cy = cy * maxr;
				//angle from forward <=abs(pi) or <=abs(pi/2)
				r = sqrt(cx*cx+cy*cy);
			//inverse:
			theta = r;
		} else if (fisheyetype == 1) {//stereographic
			//forward: r=2f*tan(theta/2)
			float maxr = 2*tan(fovtheta*0.5);
				cx = cx * maxr;
				cy = cy * maxr;
				r = sqrt(cx*cx+cy*cy);
			//inverse:
			theta = 2*atan(r*0.5);
		} else if (fisheyetype == 2) {//orthographic
			//this projection has a mathematical limit at hemisphere
			fovtheta = min(fovtheta, M_PI*0.5);
		
			//forward: r=f*sin(theta)
			float maxr = sin(fovtheta);
				cx = cx * maxr;
				cy = cy * maxr;
				r = sqrt(cx*cx+cy*cy);
			//inverse:
			theta = asin(r);
		} else if (fisheyetype == 3) {//equisolid
			//forward: r=2f*sin(theta/2)
			float maxr = 2*sin(fovtheta*0.5);
				cx = cx * maxr;
				cy = cy * maxr;
				r = sqrt(cx*cx+cy*cy);
			//inverse:
			theta = 2*asin(r*0.5);
		} else if (fisheyetype == 4) {//thoby
			//forward: r=1.47*f*sin(0.713*theta)
			float maxr = 1.47*sin(0.713*fovtheta);
				cx = cx * maxr;
				cy = cy * maxr;
				r = sqrt(cx*cx+cy*cy);
			//inverse:
			theta = asin(r/1.47)/0.713;
		}

		//rotate ray
		float s = sin(theta);
		float x = (cx)/r*s, y = (cy)/r*s, z = cos(theta);
		
		//other-handed coordinate system
		ray.x = x; ray.y = y; ray.z = -z;

		//find which side to use\n
		if (abs(ray.x) > abs(ray.y)) {
			if (abs(ray.x) > abs(ray.z)) {
				if (ray.x > 0) {
					//right\n
					float x = ray.z / ray.x;
					float y = ray.y / ray.x;
					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//left\n
					float x = -ray.z / -ray.x;
					float y = ray.y / -ray.x;
					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			} else {
				if (ray.z > 0) {
					//back\n
					float x = -ray.x / ray.z;
					float y = ray.y / ray.z;
					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//front\n
					float x = ray.x / -ray.z;
					float y = ray.y / -ray.z;
					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			}
		} else {
			if (abs(ray.y) > abs(ray.z)) {
				if (ray.y > 0) {
					//top\n
					float x = ray.x / ray.y;
					float y = ray.z / ray.y;
					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//bottom\n
					float x = ray.x / -ray.y;
					float y = -ray.z / -ray.y;
					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			} else {
				if (ray.z > 0) {
					//back\n
					float x = -ray.x / ray.z;
					float y = ray.y / ray.z;
					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//front\n
					float x = ray.x / -ray.z;
					float y = ray.y / -ray.z;
					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			}
		}

		if (drawCursor) {
			vec2 normalAngle = cursorPos*2 - 1;
			float x = ray.x / -ray.z;
			float y = ray.y / -ray.z;
			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&
				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&
				ray.z < 0) {
				colorN[loop] = vec4(1, 1, 1, 1);
			}
		}
	}

	if (antialiasing == 16) {
	  vec4 corner[4];
	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);
	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);
	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);
	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);
	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);
	}
	else if (antialiasing == 4) {
		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);
	}
	else { //if antialiasing == 1
		color = colorN[0];
	}
}
