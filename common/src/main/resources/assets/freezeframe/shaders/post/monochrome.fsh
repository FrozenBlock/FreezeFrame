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
    vec3 source = texture(InSampler, texCoord).rgb;
    float gray = dot(source, vec3(0.3, 0.59, 0.11));
    fragColor = vec4(vec3(gray), 1.0);
}
