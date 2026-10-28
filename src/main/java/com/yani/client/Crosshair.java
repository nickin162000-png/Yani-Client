package com.yani.client;

/** Miras personalizaveis: 18 estilos prontos + cor, tamanho, espessura, espaco, opacidade, contorno, ponto e modo dinamico. */
public class Crosshair {

    public static final String[] STYLES = {
            "Classica", "Ponto", "Cruz", "Tee", "X", "Circulo", "Alvo", "Quadrado", "Losango",
            "Setas", "Colchetes", "Pontilhada", "Diagonal", "Ponto Grande", "Chevron",
            "4 Pontos", "Barra", "Mira Dupla"
    };

    /** Cores rapidas (RGB). */
    public static final int[] PALETTE = {
            0xFFFFFF, 0x2ED573, 0xFF4757, 0xFFD32A, 0xFFA502, 0x18DCFF, 0x2D9CFF, 0x7C5CFF, 0xFF6BCB, 0x000000
    };

    private static float spread = 0F;

    public static int style() {
        int s = Config.getInt("xh.style", 0);
        return Math.max(0, Math.min(STYLES.length - 1, s));
    }

    public static float size() {
        return Config.getFloat("xh.size", 5F);
    }

    public static float thick() {
        return Config.getFloat("xh.thick", 1F);
    }

    public static float gap() {
        return Config.getFloat("xh.gap", 3F);
    }

    public static float alpha() {
        return Config.getFloat("xh.alpha", 0.9F);
    }

    public static int r() {
        return clamp255((int) Config.getFloat("xh.r", 255F));
    }

    public static int g() {
        return clamp255((int) Config.getFloat("xh.g", 255F));
    }

    public static int b() {
        return clamp255((int) Config.getFloat("xh.b", 255F));
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static int rgb() {
        return (r() << 16) | (g() << 8) | b();
    }

    public static void setRgb(int rgb) {
        Config.setFloat("xh.r", (rgb >> 16) & 255);
        Config.setFloat("xh.g", (rgb >> 8) & 255);
        Config.setFloat("xh.b", rgb & 255);
    }

    public static void reset() {
        Config.removePrefix("xh.");
    }

    /** Mira no jogo (centro da tela). O modo dinamico abre a mira ao andar ou pular. */
    public static void render(float cx, float cy) {
        float target = 0F;
        if (Config.getFlag("xh.dynamic", false)) {
            target = Math.min(5F, Hud.speed * 0.7F);
            if (Hud.airborne) target += 3F;
        }
        spread += (target - spread) * 0.2F;
        draw(cx, cy, style(), size(), thick(), gap() + spread, rgb(), alpha(),
                Config.getFlag("xh.outline", true), Config.getFlag("xh.dot", false));
    }

    /** Previa no menu com as configuracoes atuais. */
    public static void renderPreview(float cx, float cy) {
        float extra = 0F;
        if (Config.getFlag("xh.dynamic", false)) {
            extra = (float) (Math.sin(System.currentTimeMillis() / 500.0) + 1.0) * 2.5F;
        }
        draw(cx, cy, style(), size(), thick(), gap() + extra, rgb(), alpha(),
                Config.getFlag("xh.outline", true), Config.getFlag("xh.dot", false));
    }

    public static void draw(float cx, float cy, int st, float size, float th, float gap, int rgb, float alpha,
                            boolean outline, boolean dot) {
        int a = (int) (Math.max(0F, Math.min(1F, alpha)) * 255F);
        if (outline) shape(cx, cy, st, size, th, gap, 1F, RenderUtil.withAlpha(0x000000, (int) (a * 0.75F)));
        shape(cx, cy, st, size, th, gap, 0F, RenderUtil.withAlpha(rgb, a));
        if (dot && st != 1 && st != 13) {
            float d = Math.max(1F, th) / 2F + 0.25F;
            if (outline) r(cx - d, cy - d, cx + d, cy + d, 1F, RenderUtil.withAlpha(0x000000, (int) (a * 0.75F)));
            r(cx - d, cy - d, cx + d, cy + d, 0F, RenderUtil.withAlpha(rgb, a));
        }
    }

    // ---------- formas ----------

    private static void r(float x1, float y1, float x2, float y2, float o, int c) {
        RenderUtil.rect(x1 - o, y1 - o, x2 + o, y2 + o, c);
    }

    private static void ticks(float cx, float cy, float g, float len, float th, float o, int c,
                              boolean up, boolean down, boolean left, boolean right) {
        float h = th / 2F;
        if (left) r(cx - g - len, cy - h, cx - g, cy + h, o, c);
        if (right) r(cx + g, cy - h, cx + g + len, cy + h, o, c);
        if (up) r(cx - h, cy - g - len, cx + h, cy - g, o, c);
        if (down) r(cx - h, cy + g, cx + h, cy + g + len, o, c);
    }

    private static void shape(float cx, float cy, int st, float size, float th, float gap, float o, int c) {
        float h = th / 2F;
        float lt = th + 2F * o;
        switch (st) {
            case 0: // Classica
                ticks(cx, cy, gap, size, th, o, c, true, true, true, true);
                break;
            case 1: // Ponto
                RenderUtil.circle(cx, cy, Math.max(1F, th * 1.1F) + o, c);
                break;
            case 2: // Cruz completa
                r(cx - size, cy - h, cx + size, cy + h, o, c);
                r(cx - h, cy - size, cx + h, cy + size, o, c);
                break;
            case 3: // Tee (sem a ponta de cima)
                ticks(cx, cy, gap, size, th, o, c, false, true, true, true);
                break;
            case 4: { // X
                float s = size * 0.7F;
                RenderUtil.line(cx - s, cy - s, cx + s, cy + s, lt, c);
                RenderUtil.line(cx - s, cy + s, cx + s, cy - s, lt, c);
                break;
            }
            case 5: // Circulo
                RenderUtil.ring(cx, cy, size * 0.6F + gap * 0.5F, lt, c);
                break;
            case 6: { // Alvo: anel com marcas cruzando
                float rad = size * 0.6F + gap * 0.5F;
                float g2 = Math.max(0F, rad - size * 0.4F);
                RenderUtil.ring(cx, cy, rad, lt, c);
                ticks(cx, cy, g2, size * 0.9F, th, o, c, true, true, true, true);
                break;
            }
            case 7: { // Quadrado
                float hf = size * 0.5F + gap * 0.5F;
                r(cx - hf, cy - hf, cx + hf, cy - hf + th, o, c);
                r(cx - hf, cy + hf - th, cx + hf, cy + hf, o, c);
                r(cx - hf, cy - hf + th, cx - hf + th, cy + hf - th, o, c);
                r(cx + hf - th, cy - hf + th, cx + hf, cy + hf - th, o, c);
                break;
            }
            case 8: { // Losango
                float rad = size * 0.6F + gap * 0.5F;
                RenderUtil.line(cx, cy - rad, cx + rad, cy, lt, c);
                RenderUtil.line(cx + rad, cy, cx, cy + rad, lt, c);
                RenderUtil.line(cx, cy + rad, cx - rad, cy, lt, c);
                RenderUtil.line(cx - rad, cy, cx, cy - rad, lt, c);
                break;
            }
            case 9: { // Setas apontando para o centro
                float a = size * 0.6F;
                float g = gap;
                RenderUtil.line(cx - g, cy, cx - g - a, cy - a, lt, c);
                RenderUtil.line(cx - g, cy, cx - g - a, cy + a, lt, c);
                RenderUtil.line(cx + g, cy, cx + g + a, cy - a, lt, c);
                RenderUtil.line(cx + g, cy, cx + g + a, cy + a, lt, c);
                RenderUtil.line(cx, cy - g, cx - a, cy - g - a, lt, c);
                RenderUtil.line(cx, cy - g, cx + a, cy - g - a, lt, c);
                RenderUtil.line(cx, cy + g, cx - a, cy + g + a, lt, c);
                RenderUtil.line(cx, cy + g, cx + a, cy + g + a, lt, c);
                break;
            }
            case 10: { // Colchetes nos cantos
                float rad = size * 0.5F + gap * 0.5F;
                float arm = size * 0.45F;
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        float px = cx + sx * rad;
                        float py = cy + sy * rad;
                        float hx = px - sx * arm;
                        float vy = py - sy * arm;
                        r(Math.min(px, hx), py - h, Math.max(px, hx), py + h, o, c);
                        r(px - h, Math.min(py, vy), px + h, Math.max(py, vy), o, c);
                    }
                }
                break;
            }
            case 11: { // Pontilhada
                float step = size / 3F + th;
                float hs = Math.max(0.5F, th * 0.7F);
                for (int i = 0; i < 3; i++) {
                    float d = gap + i * step;
                    r(cx - d - hs, cy - hs, cx - d + hs, cy + hs, o, c);
                    r(cx + d - hs, cy - hs, cx + d + hs, cy + hs, o, c);
                    r(cx - hs, cy - d - hs, cx + hs, cy - d + hs, o, c);
                    r(cx - hs, cy + d - hs, cx + hs, cy + d + hs, o, c);
                }
                break;
            }
            case 12: { // Diagonal com espaco
                float e = gap * 0.7F;
                float d = size * 0.7F;
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        RenderUtil.line(cx + sx * e, cy + sy * e, cx + sx * (e + d), cy + sy * (e + d), lt, c);
                    }
                }
                break;
            }
            case 13: // Ponto grande
                RenderUtil.circle(cx, cy, Math.max(1.5F, size * 0.35F) + o, c);
                break;
            case 14: { // Chevron ^
                float a = size * 0.7F;
                RenderUtil.line(cx - a, cy + a, cx, cy, lt, c);
                RenderUtil.line(cx + a, cy + a, cx, cy, lt, c);
                break;
            }
            case 15: { // 4 pontos
                float d = gap + size * 0.5F;
                float hs = Math.max(0.5F, th * 0.8F);
                r(cx - d - hs, cy - hs, cx - d + hs, cy + hs, o, c);
                r(cx + d - hs, cy - hs, cx + d + hs, cy + hs, o, c);
                r(cx - hs, cy - d - hs, cx + hs, cy - d + hs, o, c);
                r(cx - hs, cy + d - hs, cx + hs, cy + d + hs, o, c);
                break;
            }
            case 16: // Barra horizontal
                ticks(cx, cy, gap, size * 1.4F, th, o, c, false, false, true, true);
                break;
            default: { // Mira dupla: marcas + anel externo
                ticks(cx, cy, gap, size * 0.6F, th, o, c, true, true, true, true);
                RenderUtil.ring(cx, cy, gap + size + 3F, lt, c);
                break;
            }
        }
    }
}
