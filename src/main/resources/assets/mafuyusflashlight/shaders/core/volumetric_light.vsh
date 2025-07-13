#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec3 worldPos;
out vec3 viewPos;
out float distanceFromApex;
out vec4 screenPos;

void main() {
    vec4 worldPosition = vec4(Position, 1.0);
    vec4 viewPosition = ModelViewMat * worldPosition;
    gl_Position = ProjMat * viewPosition;

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = UV2;

    // 传递世界坐标和视图坐标
    worldPos = Position;
    viewPos = viewPosition.xyz;

    // 计算从锥体顶点的距离（用于衰减）
    distanceFromApex = length(Position);

    // 屏幕空间坐标，用于深度采样
    screenPos = gl_Position;
}