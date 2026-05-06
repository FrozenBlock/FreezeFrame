#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main() {
    vec3 source = texture(InSampler, texCoord).rgb;
    float gray = dot(source, vec3(0.3, 0.59, 0.11));
    fragColor = vec4(vec3(gray), 1.0);
}
