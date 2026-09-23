#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ContrastConfig {
    float Contrast;
    float Brightness;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    color = ((color - 0.5) * Contrast) + 0.5 + Brightness;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
