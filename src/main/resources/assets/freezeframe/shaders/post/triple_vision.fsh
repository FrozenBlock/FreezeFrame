#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform OffsetConfig {
    float Offset;
};

out vec4 fragColor;

void main() {
    float minDimension = max(min(InSize.x, InSize.y), 1.0);
    vec2 offset = vec2(max(0.0, Offset) * minDimension / (512.0 * max(InSize.x, 1.0)), 0.0);
    vec3 left = texture(InSampler, clamp(texCoord - offset, 0.0, 1.0)).rgb;
    vec3 center = texture(InSampler, texCoord).rgb;
    vec3 right = texture(InSampler, clamp(texCoord + offset, 0.0, 1.0)).rgb;
    fragColor = vec4(clamp((left + center + right) / 3.0, 0.0, 1.0), 1.0);
}
