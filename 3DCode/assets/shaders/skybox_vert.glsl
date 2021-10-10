#version 330 core

in vec3 position;
out vec3 textureCoords;

uniform mat4 view_matrix;
uniform mat4 projection_matrix;

void main(void){

    gl_Position = projection_matrix * view_matrix * vec4(position, 1.0);
    textureCoords = position;

}