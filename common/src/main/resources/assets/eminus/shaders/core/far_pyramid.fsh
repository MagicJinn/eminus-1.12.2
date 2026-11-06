#version 330

#moj_import <eminus:far_depth.glsl>

uniform sampler2D Source;

out vec4 fragColor;

void main() {
    ivec2 size = textureSize(Source, 0);
    ivec2 first = ivec2(gl_FragCoord.xy) * BLOCK;
    ivec2 last = min(first + ivec2(BLOCK - 1), size - 1);
    float farthest = NEAREST;
    for (int y = first.y; y <= last.y; y++) {
        for (int x = first.x; x <= last.x; x++) {
            float depth = texelFetch(Source, ivec2(x, y), 0).r;
            if (NEARER(farthest, depth)) {
                farthest = depth;
            }
        }
    }
    fragColor = vec4(farthest, 0.0, 0.0, 0.0);
}
