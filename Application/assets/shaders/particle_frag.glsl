#version 330 core

in vec2 textureCoords;

out vec4 color;
uniform sampler2D texture2D;
uniform float blend;

uniform int imageCount;
uniform float offsetImage;

void main(void){

    if(offsetImage != imageCount -1){
        //Blend

        vec4 colorFirst = texture(texture2D, textureCoords);
        vec4 colorSec = texture(texture2D, vec2(textureCoords.x + 1.0/imageCount, textureCoords.y));

        color = mix(colorFirst,colorSec,blend);
    }else{
        //Last texture
        color = mix(texture(texture2D, textureCoords), vec4(0.0f),blend);
    }
}
