package com.yani.client;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    private static void begin2D() {
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_179129_p();
        GlStateManager.func_179120_a(770, 771, 1, 0);
    }

    private static void end2D() {
        GlStateManager.func_179098_w();
        GlStateManager.func_179141_d();
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    public static void setColor(int argb) {
        float a = (argb >>> 24 & 255) / 255F;
        float r = (argb >> 16 & 255) / 255F;
        float g = (argb >> 8 & 255) / 255F;
        float b = (argb & 255) / 255F;
        GlStateManager.func_179131_c(r, g, b, a);
    }

    public static void rect(float x, float y, float x2, float y2, int color) {
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y);
        GL11.glEnd();
        end2D();
    }

    public static void roundedRect(float x, float y, float x2, float y2, float r, int color) {
        r = Math.min(r, Math.min((x2 - x) / 2F, (y2 - y) / 2F));
        if (r <= 0.5F) {
            rect(x, y, x2, y2, color);
            return;
        }
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f((x + x2) / 2F, (y + y2) / 2F);
        int seg = 8;
        float[][] corners = {
                {x + r, y + r, 180F},
                {x2 - r, y + r, 270F},
                {x2 - r, y2 - r, 0F},
                {x + r, y2 - r, 90F}
        };
        for (float[] c : corners) {
            for (int i = 0; i <= seg; i++) {
                double ang = Math.toRadians(c[2] + 90.0 * i / seg);
                GL11.glVertex2d(c[0] + Math.cos(ang) * r, c[1] + Math.sin(ang) * r);
            }
        }
        GL11.glVertex2d(x + r + Math.cos(Math.toRadians(180)) * r, y + r + Math.sin(Math.toRadians(180)) * r);
        GL11.glEnd();
        end2D();
    }

    public static void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x3, y3);
        GL11.glEnd();
        end2D();
    }

    /** Retangulo com degrade vertical (c1 em cima, c2 embaixo). */
    public static void gradient(float x, float y, float x2, float y2, int c1, int c2) {
        begin2D();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f((c1 >> 16 & 255) / 255F, (c1 >> 8 & 255) / 255F, (c1 & 255) / 255F, (c1 >>> 24 & 255) / 255F);
        GL11.glVertex2f(x2, y);
        GL11.glVertex2f(x, y);
        GL11.glColor4f((c2 >> 16 & 255) / 255F, (c2 >> 8 & 255) / 255F, (c2 & 255) / 255F, (c2 >>> 24 & 255) / 255F);
        GL11.glVertex2f(x, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GlStateManager.func_179131_c(0F, 0F, 0F, 0F);
        end2D();
    }

    public static void circle(float cx, float cy, float r, int color) {
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 28; i++) {
            double ang = Math.PI * 2 * i / 28;
            GL11.glVertex2d(cx + Math.cos(ang) * r, cy + Math.sin(ang) * r);
        }
        GL11.glEnd();
        end2D();
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0F, Math.min(1F, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, int alpha) {
        return ((alpha & 255) << 24) | (color & 0xFFFFFF);
    }

    public static boolean hover(int mx, int my, float x, float y, float x2, float y2) {
        return mx >= x && mx <= x2 && my >= y && my <= y2;
    }

    public static boolean hover(float mx, float my, float x, float y, float x2, float y2) {
        return mx >= x && mx <= x2 && my >= y && my <= y2;
    }

    /** Linha com espessura (retangulo girado). */
    public static void line(float x1, float y1, float x2, float y2, float thick, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001F) return;
        float nx = -dy / len * thick / 2F;
        float ny = dx / len * thick / 2F;
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x1 + nx, y1 + ny);
        GL11.glVertex2f(x1 - nx, y1 - ny);
        GL11.glVertex2f(x2 - nx, y2 - ny);
        GL11.glVertex2f(x2 + nx, y2 + ny);
        GL11.glEnd();
        end2D();
    }

    /** Anel (circulo vazado) centrado em cx, cy com raio medio r. */
    public static void ring(float cx, float cy, float r, float thick, int color) {
        float ro = r + thick / 2F;
        float ri = Math.max(0F, r - thick / 2F);
        begin2D();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        int seg = 40;
        for (int i = 0; i <= seg; i++) {
            double a = Math.PI * 2 * i / seg;
            double cs = Math.cos(a);
            double sn = Math.sin(a);
            GL11.glVertex2d(cx + cs * ro, cy + sn * ro);
            GL11.glVertex2d(cx + cs * ri, cy + sn * ri);
        }
        GL11.glEnd();
        end2D();
    }

    /** Contorno retangular de espessura t (para dentro). */
    public static void outline(float x, float y, float x2, float y2, float t, int color) {
        rect(x, y, x2, y + t, color);
        rect(x, y2 - t, x2, y2, color);
        rect(x, y + t, x + t, y2 - t, color);
        rect(x2 - t, y + t, x2, y2 - t, color);
    }
}
