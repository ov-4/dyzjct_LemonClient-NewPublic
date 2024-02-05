package dev.lemonclient.renderer;

import dev.lemonclient.LemonClient;

import static org.lwjgl.opengl.GL32C.*;

public class Framebuffer {
    private int id;
    public int texture;
    public double sizeMulti = 1; // Multiplier for the size of the framebuffer
    public int width, height; // Dimensions of the framebuffer

    /**
     * Creates a new framebuffer with a custom size multiplier
     *
     * @param sizeMulti The multiplier for the size of the framebuffer
     */
    public Framebuffer(double sizeMulti) {
        this.sizeMulti = sizeMulti;
        init();
    }

    public Framebuffer() {
        init();
    }

    private void init() {
        id = GL.genFramebuffer();
        bind();

        texture = GL.genTexture();
        GL.bindTexture(texture);
        GL.defaultPixelStore();

        GL.textureParam(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GL.textureParam(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GL.textureParam(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL.textureParam(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Set the width and height of the framebuffer
        width = (int) (LemonClient.mc.getWindow().getFramebufferWidth() * sizeMulti);
        height = (int) (LemonClient.mc.getWindow().getFramebufferHeight() * sizeMulti);

        GL.textureImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null);
        GL.framebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

        unbind();
    }

    public void bind() {
        GL.bindFramebuffer(id);
    }

    /**
     * Sets the viewport to the size of the framebuffer
     */
    public void setViewport() {
        GL.viewport(0, 0, width, height);
    }

    public void unbind() {
        LemonClient.mc.getFramebuffer().beginWrite(false);
    }

    public void resize() {
        GL.deleteFramebuffer(id);
        GL.deleteTexture(texture);

        init();
    }
}
