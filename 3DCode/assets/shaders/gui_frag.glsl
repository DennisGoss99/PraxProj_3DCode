#version 330 core

in vec2 textureCoords;

out vec4 color;

uniform sampler2D texture2D;

void main(void){

    color = texture(texture2D,textureCoords);

}
