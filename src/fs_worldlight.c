#version 330 core

out vec4 fragColor;
float switchWorldState;

void main() {
  fragColor = vec4(1.0f) * vec4(switchWorldState);
}
