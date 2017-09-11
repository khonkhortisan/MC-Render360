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

//uniform float aspectratio;

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
		float aspectratio=1440.0/900.0; //FIXME
		if (aspectratio > 1.0) {
			cx = cx * aspectratio;
		} else {
			cy = cy * aspectratio;
		}
		//only draw center circle
		if (cx*cx+cy*cy > 1) {
			color = backgroundColor;
			return;
		}
		
		//decrease fov by slider
		float limitedfov = fovx;
		//and by projection limit
		if (fisheyetype == 2) {//orthographic [-1..1] -> [-0.5..0.5]
			limitedfov = min(limitedfov, 180);
		}
		cx = cx * limitedfov/360;
		cy = cy * limitedfov/360;
		
		//scale to angle (equidistant) [-1..1] -> [-pi..pi] (orthographic [-0.5..0.5] -> [-pi/2..pi/2]
		cx = cx * M_PI;
		cy = cy * M_PI;
		
		//angle from forward <=abs(pi) or <=abs(pi/2)
		float r = sqrt(cx*cx+cy*cy);

		//if (fisheyetype == 0) {//equidistant
			float theta = r;
		if (fisheyetype == 1) {//stereographic
			theta = 2*atan(r/2);
		} else if (fisheyetype == 2) {//orthographic
			theta = asin(r);
		} else if (fisheyetype == 3) {//equisolid
			theta = 2*asin(r/2);
		} else if (fisheyetype == 4) {//thoby
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
