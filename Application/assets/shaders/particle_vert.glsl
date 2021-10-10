#version 330 core

in vec2 position;

out vec2 textureCoords;

uniform mat4 projection_matrix;
uniform mat4 viewModel_matrix;

uniform int imageCount;
uniform float offsetImage;

void main(void){

    gl_Position = projection_matrix * viewModel_matrix * vec4(position, 0.0, 1.0);
    vec2 transformedPosition = vec2((position.x + 1.0) / 2.0, (position.y + 1.0) / 2.0);

    //set width to 1 imageWidth + offset
    transformedPosition.x = (transformedPosition.x / imageCount) + offsetImage/imageCount;

    textureCoords = transformedPosition;

}
