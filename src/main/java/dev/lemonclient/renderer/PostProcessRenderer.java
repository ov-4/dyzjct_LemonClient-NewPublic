package dev.lemonclient.renderer;

import dev.lemonclient.enums.DrawMode;
import dev.lemonclient.utils.PreInit;
import net.minecraft.client.util.math.MatrixStack;

public class PostProcessRenderer {
    private static Mesh mesh;
    private static final MatrixStack matrices = new MatrixStack();

    @PreInit
    public static void init() {
        mesh = new Mesh(DrawMode.Triangles, Mesh.Attrib.Vec2);
        mesh.begin();

        mesh.quad(
            mesh.vec2(-1, -1).next(),
            mesh.vec2(-1, 1).next(),
            mesh.vec2(1, 1).next(),
            mesh.vec2(1, -1).next()
        );

        mesh.end();
    }

    public static void beginRender() {
        mesh.beginRender(matrices, 1.0f);
    }

    public static void render() {
        mesh.render(matrices, 1.0f);
    }

    public static void endRender() {
        mesh.endRender();
    }
}
