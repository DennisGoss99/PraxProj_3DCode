#version 330 core

#define MAX_POINTLIGHTS 4
#define MAX_SPOTLIGHTS 4

#define UseBlinnPhong true

//input from vertex shader
in struct VertexData
{
    vec3 position;
    vec2 texcoord;
    float originAngle;
    vec3 normal;
    vec3 pointLightDir[MAX_POINTLIGHTS];
    vec3 spotLightDir[MAX_SPOTLIGHTS];
} vertexData;

//Uniforms
uniform sampler2D diff;
uniform sampler2D emit;
uniform sampler2D spec;

uniform sampler2D overlay;

uniform vec3 emitColor;
uniform float shininess;

//Pointlight Uniforms
    uniform int pointLightSize;

    uniform vec3 pointLightColors[MAX_POINTLIGHTS];
    uniform vec3 pointLightAttenuation;

//SpotLight Uniforms
    uniform int spotLightSize;

    uniform vec3 spotLightColors[MAX_SPOTLIGHTS];

    uniform float spotLightInnerAngles[MAX_SPOTLIGHTS];
    uniform float spotLightOuterAngles[MAX_SPOTLIGHTS];

    uniform vec3 spotLightDirections[MAX_SPOTLIGHTS];

    uniform vec3 spotLightAttenuation;
//
    uniform float time;
    uniform int useOverlay;

//fragment shader output
out vec4 color;

void main(){

    vec4 diffTexture = texture(diff, vertexData.texcoord);
    vec4 emitTexture = texture(emit, vertexData.texcoord);
    vec4 specTexture = texture(spec, vertexData.texcoord);

    // set emmitColor
    emitTexture = emitTexture * vec4(emitColor,0.0f);

    // normalize everything necessary //
    vec3 n = normalize(vertexData.normal);
    vec3 viewDirection = vertexData.position.xyz;


    // PointLight
    vec4 diffusePointsSum;
    for(int i = 0; i < pointLightSize; i++)
    {
        float d_point = length(vertexData.pointLightDir[i]);
        float fatt_point = 1.0f / (pointLightAttenuation.x + pointLightAttenuation.y * d_point + pointLightAttenuation.z * (d_point * d_point));

        vec3 l_point = normalize(vertexData.pointLightDir[i]);

        // diffuse component //
        float cos_a_point = max(0.0, dot(n, l_point));
        vec3 diffuseTerm_point = diffTexture.xyz ;


        // spec matierial
        float spec_point = 0.0f;
        vec3 R_point;
        if(UseBlinnPhong){
            // BLINN-PHONG
            R_point = normalize(normalize(vertexData.pointLightDir[i]) + normalize(viewDirection));
            spec_point = pow(max(dot(n, R_point),0.0), shininess);
        }else{
            // PHONG
            R_point = normalize(reflect(-normalize(vertexData.pointLightDir[i]),n));
            spec_point = pow(max(dot(R_point, normalize(viewDirection)),0.0), shininess);
        }

        vec4 spec_pointTexture = specTexture * spec_point;

        diffusePointsSum += (vec4(diffuseTerm_point * cos_a_point, 1.0f) + spec_pointTexture) * fatt_point * vec4( pointLightColors[i], 1.0f);
    }


    // SpotLight
    vec4 diffuseSpotSum;
    for(int i = 0; i < spotLightSize; i++)
    {
        float d_spot = length(vertexData.spotLightDir[i]);
        float fatt_spot = 1.0f / (spotLightAttenuation.x + spotLightAttenuation.y * d_spot + spotLightAttenuation.z * (d_spot * d_spot));

        vec3 l_spot = normalize(vertexData.spotLightDir[i]);
        float cos_a_spot = max(0.0, dot(n, l_spot));
        vec3 diffuseTerm_spot = diffTexture.xyz;

        // spec matierial
        float spec_spot = 0.0f;
        vec3 R_spot;
        if(UseBlinnPhong){
            // BLINN-PHONG
            R_spot = normalize(normalize(vertexData.spotLightDir[i]) + normalize(viewDirection));
            spec_spot = pow(max(dot(n, R_spot),0.0), shininess);
        }else{
            // PHONG
            R_spot = normalize(reflect(-normalize(vertexData.spotLightDir[i]),n));
            spec_spot = pow(max(dot(R_spot, normalize(viewDirection)),0.0), shininess);
        }

        vec4 spec_pointTexture = specTexture * spec_spot;

        vec4 diffuse_spot =  (vec4(diffuseTerm_spot * cos_a_spot, 1.0f) + spec_pointTexture) * vec4(spotLightColors[i], 1.0f) * fatt_spot;

        float theta = max(0.0, dot(normalize(spotLightDirections[i]), -l_spot));
        float intensity = 0;

        if (theta > cos(spotLightInnerAngles[i]))
            intensity = 1;
        else if (theta > cos(spotLightOuterAngles[i]))
            intensity = ((theta - cos(spotLightOuterAngles[i])) / (cos(spotLightInnerAngles[i]) - cos(spotLightOuterAngles[i])));
        else
            intensity = 0;

        diffuseSpotSum += diffuse_spot * intensity;

    }


    vec4 ambiantLight = diffTexture * min(1.0f, max(0.2f,vertexData.originAngle));

    color = emitTexture + ambiantLight + diffusePointsSum + diffuseSpotSum ;

}

