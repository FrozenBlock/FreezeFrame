#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

vec2 toSquareSpace(vec2 uv) {
    float minDimension = max(min(InSize.x, InSize.y), 1.0);
    vec2 scale = InSize / minDimension;
    return (uv - 0.5) * scale + 0.5;
}

vec2 fromSquareSpace(vec2 squareUv) {
    float minDimension = max(min(InSize.x, InSize.y), 1.0);
    vec2 scale = InSize / minDimension;
    return (squareUv - 0.5) / scale + 0.5;
}

float lensMask(vec2 uv, vec2 center, vec2 radius) {
    vec2 p = (uv - center) / radius;
    float d = dot(p, p);
    float t = clamp((1.0 - d) / 0.22, 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

vec3 lensSample(vec2 uv, vec2 center, vec2 radius) {
    vec2 local = (uv - center) / radius;
    vec2 sampleUv = clamp(fromSquareSpace(vec2(0.5) + local * vec2(0.42, 0.32)), 0.0, 1.0);
    return texture(InSampler, sampleUv).rgb;
}

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec2 squareCoord = toSquareSpace(texCoord);
    vec3 color = base * vec3(0.18, 0.12, 0.14);
    float total = 0.0;

    vec2 centers[6] = vec2[](
        vec2(0.10, 0.28),
        vec2(0.50, 0.24),
        vec2(0.90, 0.28),
        vec2(0.20, 0.74),
        vec2(0.50, 0.78),
        vec2(0.80, 0.74)
    );

    for (int i = 0; i < 6; i++) {
        vec2 radius = vec2(0.5, 0.45);
        float mask = lensMask(squareCoord, centers[i], radius);
        vec3 sampled = lensSample(squareCoord, centers[i], radius);
        color += sampled * mask;
        total += mask;
    }

    color = color / max(total, 1.0);
    color *= vec3(1.08, 0.88, 0.92);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
