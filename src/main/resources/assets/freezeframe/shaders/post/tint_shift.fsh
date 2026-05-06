#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform TintShiftConfig {
    vec4 TintColor;
    vec4 Params;
};

out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float gray = dot(color, vec3(0.3, 0.59, 0.11));
    color = mix(vec3(gray), color, Params.z);
    color = mix(color, TintColor.rgb, Params.x);
    color = ((color - 0.5) * Params.y) + 0.5;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
