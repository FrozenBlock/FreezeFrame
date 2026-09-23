#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform TintConfig {
    vec4 TintColor;
    vec4 TintMeta;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 source = texture(InSampler, texCoord).rgb;
    vec3 tint = clamp(TintColor.rgb, 0.0, 1.0);
    float mode = TintMeta.x; // 0 = mix, 1 = multiply
    float amount = clamp(TintMeta.y, 0.0, 1.0);

    vec3 mixed = mix(source, tint, amount);
    vec3 multiplied = source * tint;
    vec3 outColor = mix(mixed, multiplied, step(0.5, mode));
    fragColor = vec4(clamp(outColor, 0.0, 1.0), 1.0);
}
