package com.yani.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Fachada de texto. Usa a fonte moderna (TTF) e, se der problema ou se estiver
 * desligada no menu, volta para a fonte padrao do Minecraft.
 * Para usar sua propria fonte: coloque um arquivo font.ttf em
 * src/main/resources/assets/yaniclient/fonts/
 */
public class Fonts {
    private static final float SIZE = 10F;
    private static CustomFont regular;
    private static CustomFont bold;
    private static boolean tried = false;

    private static void init() {
        if (tried) return;
        tried = true;
        try {
            Font base = null;
            InputStream in = Fonts.class.getResourceAsStream("/assets/yaniclient/fonts/font.ttf");
            if (in != null) {
                try {
                    base = Font.createFont(Font.TRUETYPE_FONT, in);
                } finally {
                    in.close();
                }
            }
            if (base == null) {
                String[] pref = {"Segoe UI", "Helvetica Neue", "SF Pro Display", "Roboto", "Ubuntu", "Arial", "SansSerif"};
                Set<String> avail = new HashSet<String>(Arrays.asList(
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
                String pick = "SansSerif";
                for (String p : pref) {
                    if (avail.contains(p)) {
                        pick = p;
                        break;
                    }
                }
                base = new Font(pick, Font.PLAIN, 12);
            }
            regular = new CustomFont(base.deriveFont(Font.PLAIN, SIZE * CustomFont.SCALE));
            bold = new CustomFont(base.deriveFont(Font.BOLD, SIZE * CustomFont.SCALE));
        } catch (Throwable t) {
            regular = null;
            bold = null;
        }
    }

    private static boolean custom() {
        init();
        return regular != null && Config.get("Fonte Moderna");
    }

    public static float width(String text, float s, boolean isBold) {
        if (custom()) return (isBold ? bold : regular).width(text, s);
        return Minecraft.func_71410_x().field_71466_p.func_78256_a(text) * s;
    }

    /** Desenha com o topo do texto em y. */
    public static void draw(String text, float x, float y, int color, float s, boolean isBold, boolean shadow) {
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        if (custom()) {
            CustomFont f = isBold ? bold : regular;
            if (shadow) {
                int sc = ((int) ((color >>> 24) * 0.6F) << 24);
                f.draw(text, x + 0.8F * s, y + 0.8F * s, sc, s);
            }
            f.draw(text, x, y, color, s);
        } else {
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b(x, y, 0F);
            GlStateManager.func_179152_a(s, s, 1F);
            Minecraft.func_71410_x().field_71466_p.func_175065_a(text, 0, 0, color, shadow);
            GlStateManager.func_179121_F();
            GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
        }
    }

    /** Desenha com o texto centralizado verticalmente em cy. */
    public static void drawC(String text, float x, float cy, int color, float s, boolean isBold, boolean shadow) {
        float y;
        if (custom()) y = cy - (isBold ? bold : regular).centerOffset() * s;
        else y = cy - 4F * s;
        draw(text, x, y, color, s, isBold, shadow);
    }
}
