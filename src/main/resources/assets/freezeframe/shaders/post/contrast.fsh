#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ContrastConfig {
    float Contrast;
    float Brightness;
};

out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    color = ((color - 0.5) * Contrast) + 0.5 + Brightness;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
