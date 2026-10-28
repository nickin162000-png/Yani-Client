package com.yani.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Fonte customizada: desenha os caracteres de um atlas gerado em alta resolucao. */
public class CustomFont {
    public static final int SCALE = 2;
    private static final int ATLAS_W = 1024;
    private static final int ATLAS_H = 512;
    private static final int FIRST = 32;
    private static final int LAST = 255;

    private final int[] gx = new int[LAST + 1];
    private final int[] gy = new int[LAST + 1];
    private final int[] gw = new int[LAST + 1];
    private final int cellH;
    private final float centerOffset;
    private final DynamicTexture texture;

    public CustomFont(Font font) {
        BufferedImage img = new BufferedImage(ATLAS_W, ATLAS_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g.getFontMetrics();
        cellH = fm.getHeight() + 2;
        centerOffset = (fm.getAscent() - 0.35F * font.getSize2D()) / SCALE;
        g.setColor(Color.WHITE);

        int x = 2;
        int y = 2;
        for (int c = FIRST; c <= LAST; c++) {
            int w = Math.max(1, fm.charWidth((char) c));
            int cw = w + 4;
            if (x + cw + 2 >= ATLAS_W) {
                x = 2;
                y += cellH + 2;
            }
            if (y + cellH >= ATLAS_H) break;
            gx[c] = x;
            gy[c] = y;
            gw[c] = w;
            g.drawString(String.valueOf((char) c), x + 2, y + fm.getAscent());
            x += cw + 2;
        }
        g.dispose();

        texture = new DynamicTexture(img);
        GlStateManager.func_179144_i(texture.func_110552_b());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public float centerOffset() {
        return centerOffset;
    }

    public float width(String text, float s) {
        float w = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7') {
                i++;
                continue;
            }
            if (c < FIRST || c > LAST) c = '?';
            w += gw[c];
        }
        return w / SCALE * s;
    }

    public void draw(String text, float x, float y, int color, float s) {
        float a = (color >>> 24 & 255) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;

        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        GlStateManager.func_179144_i(texture.func_110552_b());
        GlStateManager.func_179131_c(r, g, b, a);
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(x, y, 0F);
        GlStateManager.func_179152_a(s / SCALE, s / SCALE, 1F);

        GL11.glBegin(GL11.GL_QUADS);
        float pen = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7') {
                i++;
                continue;
            }
            if (c < FIRST || c > LAST) c = '?';
            float u0 = gx[c] / (float) ATLAS_W;
            float u1 = (gx[c] + gw[c] + 4) / (float) ATLAS_W;
            float v0 = gy[c] / (float) ATLAS_H;
            float v1 = (gy[c] + cellH) / (float) ATLAS_H;
            float qx = pen - 2;
            float qx2 = qx + gw[c] + 4;
            GL11.glTexCoord2f(u0, v0);
            GL11.glVertex2f(qx, 0);
            GL11.glTexCoord2f(u0, v1);
            GL11.glVertex2f(qx, cellH);
            GL11.glTexCoord2f(u1, v1);
            GL11.glVertex2f(qx2, cellH);
            GL11.glTexCoord2f(u1, v0);
            GL11.glVertex2f(qx2, 0);
            pen += gw[c];
        }
        GL11.glEnd();

        GlStateManager.func_179121_F();
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }
}
