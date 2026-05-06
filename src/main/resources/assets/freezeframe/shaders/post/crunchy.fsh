#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CrunchConfig {
    float BlockSize;
    float Levels;
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

float posterize(float channel, float levels) {
    float normalizedLevels = max(2.0, levels);
    float stepSize = 255.0 / (normalizedLevels - 1.0);
    float source = channel * 255.0;
    return clamp(round(round(source / stepSize) * stepSize) / 255.0, 0.0, 1.0);
}

void main() {
    float blockSize = max(1.0, BlockSize) / 512.0;
    vec2 squareCoord = toSquareSpace(texCoord);
    vec2 blockCoord = floor(squareCoord / blockSize) * blockSize;
    vec2 uv = clamp(fromSquareSpace(blockCoord), 0.0, 1.0);
    vec3 source = texture(InSampler, uv).rgb;
    vec3 filtered = vec3(
        posterize(source.r, Levels),
        posterize(source.g, Levels),
        posterize(source.b, Levels)
    );
    fragColor = vec4(filtered, 1.0);
}
