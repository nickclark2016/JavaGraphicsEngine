#version 400 core

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[20];

uniform float useFakeLighting;
uniform float density;
uniform float gradient;
uniform float numberOfRows;
uniform int maxLights;
uniform vec2 offset;
uniform vec4 plane;
uniform mat4 toShadowMapSpace;

out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;
out vec4 shadowCoords;

const float shadowDistance = 100;
const float transitionDistance = 30.0;

void main(void){

	vec4 worldPosition = transformationMatrix * vec4(position,1.0);
	shadowCoords = toShadowMapSpace * worldPosition;
	
	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	pass_textureCoordinates = (textureCoordinates/numberOfRows) + offset;
	
	vec3 actualNormal = normal;
	if(useFakeLighting > 0.5){
		actualNormal = vec3(0.0,1.0,0.0);
	}
	
	surfaceNormal = (transformationMatrix * vec4(actualNormal,0.0)).xyz;
	for(int i = 0; i < 4; i++){
		toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;
	
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient));
	visibility = clamp(visibility,0.0,1.0);
	
	distance = distance - (shadowDistance - transitionDistance);
	distance = distance / transitionDistance;
	shadowCoords.w = clamp(1 - distance, 0, 1);
	
}