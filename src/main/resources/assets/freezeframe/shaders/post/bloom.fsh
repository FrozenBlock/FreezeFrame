#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BloomConfig {
    float Strength;
};

out vec4 fragColor;

void main() {
    float minDimension = max(min(InSize.x, InSize.y), 1.0);
    vec2 px = vec2(minDimension / max(InSize.x, 1.0), minDimension / max(InSize.y, 1.0)) / 512.0;
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 acc = base;
    float weight = 1.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            if (x == 0 && y == 0) {
                continue;
            }
            acc += texture(InSampler, clamp(texCoord + vec2(x, y) * px, 0.0, 1.0)).rgb * 0.2;
            weight += 0.2;
        }
    }
    vec3 bloom = acc / weight;
    fragColor = vec4(clamp(mix(base, bloom, clamp(Strength, 0.0, 1.0)), 0.0, 1.0), 1.0);
}
