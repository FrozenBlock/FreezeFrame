#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float yellowish = max(0.0, (((color.r + color.g) * 0.5) - color.b));
    float whiteish = step(180.5 / 255.0, min(color.r, min(color.g, color.b)));
    float mask = max(step(0.15, yellowish), whiteish);
    vec3 boosted = color * vec3(1.18, 1.16, 1.06);
    fragColor = vec4(clamp(mix(color, boosted, mask), 0.0, 1.0), 1.0);
}
