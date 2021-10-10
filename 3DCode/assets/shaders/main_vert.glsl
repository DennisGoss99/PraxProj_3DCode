#version 330 core

#define MAX_POINTLIGHTS 4
#define MAX_SPOTLIGHTS 4

layout(location = 0) in vec3 position;
layout(location = 2) in vec3 normal;
layout(location = 1) in vec2 texcoords;


//uniforms
// translation object to world
uniform mat4 model_matrix;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;

uniform vec2 tcMultiplier;

uniform int pointLightSize;
uniform vec3 pointLightPositions[MAX_POINTLIGHTS];

uniform int spotLightSize;
uniform vec3 spotLightPositions[MAX_SPOTLIGHTS];

out struct VertexData
{
    vec3 position;
    vec2 texcoord;
    float originAngle;
    vec3 normal;
    vec3 pointLightDir[MAX_POINTLIGHTS];
    vec3 spotLightDir[MAX_SPOTLIGHTS];
} vertexData;

//
void main(){

    mat4 modelView = view_matrix * model_matrix;
    gl_Position =  projection_matrix * modelView * vec4(position, 1.0f);

    vertexData.position = -(modelView * vec4(position, 1.0f)).xyz;
    vertexData.normal = (transpose(inverse(modelView)) * vec4(normal, 0.0f)).xyz;
    vertexData.texcoord = texcoords * tcMultiplier;


    for(int i = 0; i < spotLightSize; i++)
    {
        vertexData.spotLightDir[i] = (( view_matrix * vec4(spotLightPositions[i], 1.0f)) - (modelView * vec4(position, 1.0f))).xyz;
    }

    for(int i = 0; i < pointLightSize; i++)
    {
        vertexData.pointLightDir[i] = (( view_matrix * vec4(pointLightPositions[i], 1.0f)) - (modelView * vec4(position, 1.0f) )).xyz;
    }

    vec3 cosMidpoint = (( view_matrix * vec4(0.0f, 0.0f, 0.0f, 1.0f)) - (modelView * vec4(position, 1.0f) )).xyz;
    vertexData.originAngle = max(0.0, dot(normalize(vertexData.normal), normalize(cosMidpoint)) + 0.70f);

}
