#version 150

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec3 worldPos;
in vec3 viewPos;
in float distanceFromApex;
in vec4 screenPos;

out vec4 fragColor;

// 体积光照参数
uniform vec3 LightPosition;
uniform vec3 LightDirection;
uniform float LightIntensity;
uniform float LightRange;
uniform float ConeAngle;
uniform float Time;
uniform vec2 ScreenSize;

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

// 改进的噪声函数
float noise3D(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
}

// 分层噪声
float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 4; i++) {
        value += amplitude * noise3D(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value;
}

void main() {
    // 计算屏幕空间UV坐标
    vec2 screenUV = (screenPos.xy / screenPos.w) * 0.5 + 0.5;

    // 采样场景深度
    float sceneDepth = texture(DepthSampler, screenUV).r;

    // 将深度转换为线性深度
    float near = 0.1;
    float far = 1000.0;
    float linearSceneDepth = (2.0 * near) / (far + near - sceneDepth * (far - near));
    float linearFragDepth = (2.0 * near) / (far + near - gl_FragCoord.z * (far - near));

    // 遮挡检测
    float occlusionFactor = 1.0;
    if (linearFragDepth > linearSceneDepth + 0.001) {
        occlusionFactor = 0.0; // 被遮挡
    }

    // 改进的距离衰减
    float normalizedDistance = distanceFromApex;
    float distanceAttenuation = 1.0 / (1.0 + normalizedDistance * normalizedDistance * 0.5);

    // 锥形衰减 - 基于顶点颜色的alpha通道
    float coneAttenuation = vertexColor.a;

    // 体积散射效果 - 使用分层噪声
    vec3 noiseCoord = worldPos * 0.1 + vec3(Time * 0.03);
    float scattering = fbm(noiseCoord) * 0.4 + 0.6;

    // 添加细节噪声
    vec3 detailCoord = worldPos * 0.5 + vec3(Time * 0.1);
    float detail = noise3D(detailCoord) * 0.2 + 0.8;
    scattering *= detail;

    // 边缘衰减效果
    float edgeFade = smoothstep(0.0, 0.3, normalizedDistance) *
    smoothstep(1.0, 0.7, normalizedDistance);

    // 最终强度计算
    float finalIntensity = LightIntensity * distanceAttenuation * coneAttenuation *
    scattering * occlusionFactor * edgeFade * 0.6;

    // 体积光颜色 - 更真实的暖白色
    vec3 lightColor = vec3(1.0, 0.9, 0.7);

    // 添加动态颜色变化
    lightColor.r += sin(Time * 0.7 + worldPos.x) * 0.02;
    lightColor.g += cos(Time * 0.5 + worldPos.z) * 0.015;

    // 根据距离调整颜色温度
    float colorTemp = 1.0 - normalizedDistance * 0.3;
    lightColor *= colorTemp;

    // 输出颜色
    fragColor = vec4(lightColor * finalIntensity, finalIntensity * 0.3);
}