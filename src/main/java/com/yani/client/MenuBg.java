package com.yani.client;

import java.util.Random;

/** Cenas animadas da tela inicial, uma para cada tema (0 = Meia-Noite, 1 = Abobora Maldita, 2 = Gelo Eterno). */
public class MenuBg {
    private static final int N = 90;
    /** x, y, tamanho, velocidade, fase */
    private static final float[][] P = new float[N][5];
    private static final float[] BUILD = new float[32];

    static {
        Random r = new Random(42);
        for (float[] p : P) {
            p[0] = r.nextFloat();
            p[1] = r.nextFloat();
            p[2] = 0.5F + r.nextFloat() * 1.6F;
            p[3] = 0.04F + r.nextFloat() * 0.12F;
            p[4] = r.nextFloat() * 6.2831F;
        }
        for (int i = 0; i < BUILD.length; i++) BUILD[i] = r.nextFloat();
    }

    public static int style() {
        return Theme.index();
    }

    public static void full(int style, float w, float h, int accent) {
        float t = (System.currentTimeMillis() % 3600000L) / 1000F;
        scene(style, 0, 0, w, h, t, accent, false);
    }

    /** Miniatura da cena (usada na lista de temas). */
    public static void thumb(int style, float x, float y, float w, float h, int accent) {
        scene(style, x, y, w, h, 14F, accent, true);
    }

    private static void scene(int style, float x, float y, float w, float h, float t, int accent, boolean mini) {
        Theme.T th = Theme.get(style);
        RenderUtil.gradient(x, y, x + w, y + h, th.menuTop, th.menuBottom);
        float u = h / 300F;
        if (style == 1) halloween(x, y, w, h, t, u, mini);
        else if (style == 2) ice(x, y, w, h, t, u, mini);
        else night(x, y, w, h, t, u, accent, mini);
    }

    private static float sin(double v) {
        return (float) Math.sin(v);
    }

    private static float cos(double v) {
        return (float) Math.cos(v);
    }

    private static float th(float v) {
        return Math.max(1F, v);
    }

    private static float rad(float v) {
        return Math.max(0.6F, v);
    }

    // ---------------------------------------------------------------- Meia-Noite

    private static void night(float x, float y, float w, float h, float t, float u, int accent, boolean mini) {
        if (!mini) {
            RenderUtil.circle(x + w * (0.25F + 0.10F * sin(t * 0.30)), y + h * 0.60F, 190 * u, RenderUtil.withAlpha(accent, 0x14));
            RenderUtil.circle(x + w * (0.80F + 0.08F * cos(t * 0.25)), y + h * 0.35F, 150 * u, RenderUtil.withAlpha(0xFF2D9CFF, 0x10));
        }
        int stars = mini ? 28 : N;
        for (int i = 0; i < stars; i++) {
            float[] p = P[i];
            float tw = 0.5F + 0.5F * sin(t * (0.8F + p[3] * 6F) + p[4]);
            int a = (int) (40 + 160 * tw);
            RenderUtil.circle(x + p[0] * w, y + p[1] * h * 0.72F, rad(p[2] * 0.6F * Math.max(u, 0.6F)), RenderUtil.withAlpha(0xFFFFFF, a));
        }

        // lua
        float mx = x + w * 0.80F;
        float my = y + h * 0.20F;
        float mr = 26 * u;
        if (!mini) {
            RenderUtil.circle(mx, my, mr * 2.6F, 0x0AFFFFFF);
            RenderUtil.circle(mx, my, mr * 1.8F, 0x12FFFFFF);
        }
        RenderUtil.circle(mx, my, rad(mr), 0xFFE8ECFF);
        if (!mini) {
            RenderUtil.circle(mx - mr * 0.3F, my - mr * 0.2F, mr * 0.22F, 0x40A0A8D0);
            RenderUtil.circle(mx + mr * 0.25F, my + mr * 0.3F, mr * 0.16F, 0x40A0A8D0);
        }

        // estrela cadente
        if (!mini) {
            float ph = (t % 9F) / 1.4F;
            if (ph < 1F) {
                float sx = x + w * (0.85F - ph * 0.55F);
                float sy = y + h * (0.04F + ph * 0.32F);
                int a = (int) (200 * (1F - ph));
                RenderUtil.line(sx + 46 * u, sy - 23 * u, sx, sy, th(1.6F * u), RenderUtil.withAlpha(0xFFFFFF, a / 3));
                RenderUtil.circle(sx, sy, th(1.4F * u), RenderUtil.withAlpha(0xFFFFFF, a));
            }
        }

        // cidade
        int n = mini ? 14 : 26;
        float bw = w / n;
        for (int i = 0; i < n; i++) {
            float bh = h * (0.07F + 0.22F * BUILD[i % BUILD.length]);
            float bx = x + i * bw;
            float by = y + h - bh;
            RenderUtil.rect(bx, by, bx + bw + 0.5F, y + h, 0xFF04040A);
            if (mini) continue;
            int cols = Math.max(1, (int) (bw / (7 * u + 1)));
            int rows = Math.max(1, (int) (bh / (9 * u + 1)));
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (((i * 7 + r * 3 + c * 5) % 5) != 0) continue;
                    float wx = bx + (c + 0.5F) * (bw / cols) - 1.2F * u;
                    float wy = by + 4 * u + r * 9 * u;
                    if (wy + 3 * u > y + h) continue;
                    float fl = 0.75F + 0.25F * sin(t * 0.9 + i + r * 2 + c);
                    RenderUtil.rect(wx, wy, wx + 2.4F * u, wy + 3 * u, RenderUtil.withAlpha(0xFFE08A, (int) (170 * fl)));
                }
            }
        }
        RenderUtil.gradient(x, y + h * 0.78F, x + w, y + h, 0x00000000, RenderUtil.withAlpha(accent, 0x22));
    }

    // ---------------------------------------------------------------- Abobora Maldita

    private static void halloween(float x, float y, float w, float h, float t, float u, boolean mini) {
        // lua laranja
        float mx = x + w * 0.74F;
        float my = y + h * 0.27F;
        float mr = 44 * u;
        if (!mini) {
            RenderUtil.circle(mx, my, mr * 2.7F, 0x10FF8A2E);
            RenderUtil.circle(mx, my, mr * 1.8F, 0x1AFFA53D);
        }
        RenderUtil.circle(mx, my, rad(mr), 0xFFFFB347);
        if (!mini) {
            RenderUtil.circle(mx - mr * 0.25F, my - mr * 0.15F, mr * 0.25F, 0x30FF7A00);
            RenderUtil.circle(mx + mr * 0.3F, my + mr * 0.25F, mr * 0.18F, 0x30FF7A00);
            // nuvens passando na frente da lua
            for (int i = 0; i < 3; i++) {
                float cx = x + ((t * (6 + i * 3) + i * 0.4F * w) % (w + 160 * u)) - 80 * u;
                float cy = y + h * (0.20F + 0.07F * i);
                RenderUtil.line(cx - 55 * u, cy, cx + 55 * u, cy, 9 * u, 0x70120508);
            }
        }

        // brasas subindo
        int embers = mini ? 8 : 34;
        for (int i = 0; i < embers; i++) {
            float[] p = P[i];
            float ex = x + w * (p[0] + 0.03F * sin(t * 0.7 + p[4]));
            float ey = y + h * (1F - ((p[1] + t * p[3] * 0.4F) % 1F));
            if (ex < x || ex > x + w) continue;
            RenderUtil.circle(ex, ey, rad(p[2] * 0.5F * Math.max(u, 0.7F)), RenderUtil.withAlpha(0xFFA030, (int) (60 + 80 * (0.5F + 0.5F * sin(t * 2 + p[4])))));
        }

        // arvores mortas
        tree(x + w * 0.07F, y + h, h * 0.42F, 1F, u);
        tree(x + w * 0.93F, y + h, h * 0.36F, -1F, u);

        // morcegos
        int bats = mini ? 3 : 7;
        for (int i = 0; i < bats; i++) {
            float speed = 22 + i * 7;
            float bx = x + ((t * speed + i * 0.19F * w) % (w + 90 * u)) - 45 * u;
            float by = y + h * (0.12F + 0.07F * (i % 4)) + 10 * u * sin(t * 1.3 + i);
            if (bx < x || bx > x + w) continue;
            bat(bx, by, 1F + 0.25F * (i % 3), t * 10F + i, u, mini);
        }

        // chao
        RenderUtil.rect(x, y + h * 0.93F, x + w, y + h, 0xFF070203);
        if (!mini) {
            for (int i = 0; i < 4; i++) {
                float fx = x + w * (0.12F + 0.25F * i) + sin(t * 0.2 + i) * 30 * u;
                RenderUtil.circle(fx, y + h * 0.95F, 70 * u, 0x0CFFB070);
            }
        }

        // aboboras
        pumpkin(x + w * 0.15F, y + h * 0.90F, 24 * u, t, 0, mini);
        pumpkin(x + w * 0.85F, y + h * 0.91F, 30 * u, t, 2, mini);
        pumpkin(x + w * 0.27F, y + h * 0.94F, 14 * u, t, 4, mini);
        pumpkin(x + w * 0.70F, y + h * 0.95F, 12 * u, t, 6, mini);
    }

    private static void tree(float bx, float by, float ht, float dir, float u) {
        int c = 0xFF070203;
        float t = th(4 * u);
        RenderUtil.line(bx, by, bx, by - ht, t, c);
        RenderUtil.line(bx, by - ht * 0.45F, bx + dir * ht * 0.30F, by - ht * 0.75F, th(2.6F * u), c);
        RenderUtil.line(bx + dir * ht * 0.30F, by - ht * 0.75F, bx + dir * ht * 0.34F, by - ht * 0.95F, th(1.6F * u), c);
        RenderUtil.line(bx, by - ht * 0.65F, bx + dir * ht * 0.22F, by - ht * 0.92F, th(2.2F * u), c);
        RenderUtil.line(bx, by - ht * 0.82F, bx + dir * ht * 0.12F, by - ht * 1.02F, th(1.6F * u), c);
        RenderUtil.line(bx, by - ht * 0.55F, bx - dir * ht * 0.18F, by - ht * 0.78F, th(2F * u), c);
    }

    private static void bat(float cx, float cy, float s, float phase, float u, boolean mini) {
        float k = s * Math.max(u, mini ? 0.25F : 0.7F);
        float flap = sin(phase);
        float tip = cy - 11 * k * flap;
        int c = 0xFF0A0306;
        RenderUtil.triangle(cx, cy - 1 * k, cx - 18 * k, tip, cx - 6 * k, cy + 5 * k, c);
        RenderUtil.triangle(cx, cy - 1 * k, cx + 18 * k, tip, cx + 6 * k, cy + 5 * k, c);
        RenderUtil.circle(cx, cy, rad(3.4F * k), c);
        RenderUtil.triangle(cx - 2.6F * k, cy - 2 * k, cx - 1.2F * k, cy - 6 * k, cx, cy - 2 * k, c);
        RenderUtil.triangle(cx + 2.6F * k, cy - 2 * k, cx + 1.2F * k, cy - 6 * k, cx, cy - 2 * k, c);
    }

    private static void pumpkin(float cx, float cy, float r, float t, int seed, boolean mini) {
        float fl = 0.78F + 0.22F * sin(t * 7 + seed * 1.7) * sin(t * 3.1 + seed);
        if (!mini) RenderUtil.circle(cx, cy - r * 0.2F, r * 2.4F, RenderUtil.withAlpha(0xFFA030, (int) (22 * fl)));
        RenderUtil.circle(cx - r * 0.5F, cy, r * 0.78F, 0xFFD85F00);
        RenderUtil.circle(cx + r * 0.5F, cy, r * 0.78F, 0xFFD85F00);
        RenderUtil.circle(cx, cy, r * 0.92F, 0xFFFF7A00);
        RenderUtil.rect(cx - r * 0.10F, cy - r * 1.18F, cx + r * 0.14F, cy - r * 0.78F, 0xFF2E5A1A);
        int face = RenderUtil.withAlpha(0xFFE08A, (int) (255 * fl));
        // olhos
        RenderUtil.triangle(cx - r * 0.55F, cy - r * 0.05F, cx - r * 0.12F, cy - r * 0.05F, cx - r * 0.34F, cy - r * 0.45F, face);
        RenderUtil.triangle(cx + r * 0.12F, cy - r * 0.05F, cx + r * 0.55F, cy - r * 0.05F, cx + r * 0.34F, cy - r * 0.45F, face);
        // boca
        RenderUtil.rect(cx - r * 0.58F, cy + r * 0.20F, cx + r * 0.58F, cy + r * 0.50F, face);
        if (!mini && r > 8) {
            for (int i = 0; i < 3; i++) {
                float tx = cx - r * 0.40F + i * r * 0.40F;
                RenderUtil.rect(tx, cy + r * 0.20F, tx + r * 0.16F, cy + r * 0.32F, 0xFF3A1400);
                RenderUtil.rect(tx + r * 0.12F, cy + r * 0.40F, tx + r * 0.28F, cy + r * 0.50F, 0xFF3A1400);
            }
        }
    }

    // ---------------------------------------------------------------- Gelo Eterno

    private static void ice(float x, float y, float w, float h, float t, float u, boolean mini) {
        // estrelas fracas
        for (int i = 0; i < (mini ? 12 : 40); i++) {
            float[] p = P[i];
            RenderUtil.circle(x + p[0] * w, y + p[1] * h * 0.45F, rad(p[2] * 0.5F * Math.max(u, 0.6F)),
                    RenderUtil.withAlpha(0xFFFFFF, (int) (50 + 90 * (0.5F + 0.5F * sin(t + p[4])))));
        }

        // aurora boreal
        int cols = mini ? 12 : 28;
        float cw = w / cols;
        for (int i = 0; i < cols; i++) {
            float top = y + h * (0.08F + 0.10F * sin(t * 0.40 + i * 0.45));
            float bot = y + h * (0.55F + 0.10F * sin(t * 0.30 + i * 0.30));
            float mid = (top + bot) / 2F;
            int col = RenderUtil.lerpColor(0xFF3CFFB0, 0xFF4D9CFF, (i / (float) cols + 0.25F * sin(t * 0.2)) % 1F);
            int hi = RenderUtil.withAlpha(col, 0x40);
            float cx = x + i * cw;
            RenderUtil.gradient(cx, top, cx + cw + 1, mid, RenderUtil.withAlpha(col, 0), hi);
            RenderUtil.gradient(cx, mid, cx + cw + 1, bot, hi, RenderUtil.withAlpha(col, 0));
        }

        // flocos de neve gigantes girando
        float fx = x + w * 0.16F;
        float fy = y + h * 0.22F;
        float fr = 22 * u;
        if (!mini) {
            for (int k = 0; k < 3; k++) {
                float a = t * 0.15F + k * 1.0472F;
                RenderUtil.line(fx - cos(a) * fr, fy - sin(a) * fr, fx + cos(a) * fr, fy + sin(a) * fr, th(1.3F * u), 0x70DFF6FF);
                RenderUtil.circle(fx + cos(a) * fr, fy + sin(a) * fr, 2F * u, 0x90DFF6FF);
                RenderUtil.circle(fx - cos(a) * fr, fy - sin(a) * fr, 2F * u, 0x90DFF6FF);
            }
            RenderUtil.circle(fx, fy, 3F * u, 0xB0FFFFFF);
        }

        // montanhas (camada de tras)
        mountain(x, y, w, h, 0.25F, 0.52F, 0.22F, 0xFF0B2236);
        mountain(x, y, w, h, 0.55F, 0.46F, 0.22F, 0xFF0D2A42);
        mountain(x, y, w, h, 0.78F, 0.55F, 0.22F, 0xFF0B2236);

        // pinheiros
        float[] pines = {0.06F, 0.12F, 0.20F, 0.80F, 0.88F, 0.94F};
        for (int i = 0; i < pines.length; i++) {
            float pw = (0.045F + 0.015F * (i % 3)) * w;
            float ph = h * (0.20F + 0.05F * (i % 3));
            pine(x + pines[i] * w, y + h * 0.95F, pw, ph);
        }

        // chao de neve
        RenderUtil.gradient(x, y + h * 0.94F, x + w, y + h, 0xFF8EC4E6, 0xFF4E88B0);

        // neve caindo
        int flakes = mini ? 22 : N;
        for (int i = 0; i < flakes; i++) {
            float[] p = P[i];
            float sx = x + w * (((p[0] + 0.04F * sin(t * 0.6 + p[4])) % 1F + 1F) % 1F);
            float sy = y + h * ((p[1] + t * p[3] * 0.55F) % 1F);
            RenderUtil.circle(sx, sy, rad(p[2] * 0.9F * Math.max(u, 0.6F)), 0xB0FFFFFF);
        }
    }

    private static void mountain(float x, float y, float w, float h, float cx, float peak, float half, int color) {
        float ax = x + w * cx;
        float ay = y + h * peak;
        float by = y + h * 0.96F;
        RenderUtil.triangle(x + w * (cx - half), by, ax, ay, x + w * (cx + half), by, color);
        float cap = h * 0.10F;
        float f = cap / (by - ay);
        float hw = w * half * f;
        RenderUtil.triangle(ax - hw, ay + cap, ax, ay, ax + hw, ay + cap, 0xFFCFEFFF);
    }

    private static void pine(float cx, float baseY, float pw, float ph) {
        int dark = 0xFF06141F;
        RenderUtil.rect(cx - pw * 0.06F, baseY - ph * 0.12F, cx + pw * 0.06F, baseY, 0xFF04101A);
        for (int i = 0; i < 3; i++) {
            float lh = ph * 0.46F;
            float top = baseY - ph * 0.12F - ph * (0.30F * i) - lh;
            float wd = pw * (1F - 0.22F * i) * 0.5F;
            RenderUtil.triangle(cx - wd, top + lh, cx, top, cx + wd, top + lh, dark);
            RenderUtil.triangle(cx - wd * 0.45F, top + lh * 0.45F, cx, top, cx + wd * 0.45F, top + lh * 0.45F, 0xFF16405C);
        }
    }
}
