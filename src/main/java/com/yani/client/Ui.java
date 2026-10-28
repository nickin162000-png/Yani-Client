package com.yani.client;

public class Ui {
    public static void button(String label, float x, float y, float w, float h, float hoverAnim, boolean primary) {
        int accent = Config.accent();
        int bg = primary
                ? RenderUtil.lerpColor(RenderUtil.withAlpha(accent, 0xB4), accent, hoverAnim)
                : RenderUtil.lerpColor(Theme.cur().btn, Theme.cur().btnHover, hoverAnim);
        RenderUtil.roundedRect(x, y, x + w, y + h, 7, bg);
        // em acento claro (branco) o texto precisa ser escuro
        int tc = (primary && isLight(accent)) ? 0xFF111114 : 0xFFFFFFFF;
        float tw = Fonts.width(label, 1F, true);
        Fonts.drawC(label, x + (w - tw) / 2F, y + h / 2F, tc, 1F, true, false);
    }

    public static boolean isLight(int argb) {
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = argb & 255;
        return (r * 299 + g * 587 + b * 114) / 1000 > 190;
    }

    public static float approach(float cur, boolean on) {
        return cur + ((on ? 1F : 0F) - cur) * 0.25F;
    }

    private static final net.minecraft.util.ResourceLocation LOGO =
            new net.minecraft.util.ResourceLocation("yaniclient", "textures/logo.png");

    /** Desenha a marca do Yani Client (png branco, pintado com a cor dada). */
    public static void logo(float x, float y, float size, int argb) {
        net.minecraft.client.Minecraft.func_71410_x().func_110434_K().func_110577_a(LOGO);
        net.minecraft.client.renderer.GlStateManager.func_179147_l();
        net.minecraft.client.renderer.GlStateManager.func_179120_a(770, 771, 1, 0);
        net.minecraft.client.renderer.GlStateManager.func_179098_w();
        net.minecraft.client.renderer.GlStateManager.func_179131_c(((argb >> 16) & 255) / 255F,
                ((argb >> 8) & 255) / 255F, (argb & 255) / 255F, ((argb >>> 24) & 255) / 255F);
        net.minecraft.client.renderer.GlStateManager.func_179094_E();
        net.minecraft.client.renderer.GlStateManager.func_179109_b(x, y, 0F);
        float k = size / 256F;
        net.minecraft.client.renderer.GlStateManager.func_179152_a(k, k, 1F);
        net.minecraft.client.gui.Gui.func_146110_a(0, 0, 0F, 0F, 256, 256, 256F, 256F);
        net.minecraft.client.renderer.GlStateManager.func_179121_F();
        net.minecraft.client.renderer.GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }
}
