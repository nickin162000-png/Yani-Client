package com.yani.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModernGui extends GuiScreen {

    private static final int PW = 540;
    private static final int PH = 340;
    private static final String[] TABS = {"HUD", "Combate", "Mira", "Utilidades", "Interface", "Aparencia", "Tamanho"};

    private static final String[] HUD_ITEMS = {"Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
            "Keystrokes", "Teclas Mouse", "Armor HUD", "Efeitos", "Velocidade", "Memoria", "Data",
            "Sessao", "Rotacao", "Grafico FPS"};
    private static final String[] COMBAT_ITEMS = {"Combo", "Reach", "TNT", "Itens", "Flash de Dano",
            "Alvo", "Hits", "Abates", "Aviso Vida Baixa"};
    private static final String[] UTIL_ITEMS = {"Toggle Sprint", "Zoom", "Fullbright",
            "Toggle Sneak", "Zoom Scroll", "Notificacoes"};
    private static final String[] IFACE_ITEMS = {"Hotbar Moderna", "Barras de Status", "Menus Modernos"};
    private static final String[] SIZE_ITEMS = {"Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
            "Keystrokes", "Teclas Mouse", "Armor HUD", "Efeitos", "Velocidade", "Memoria", "Data", "Sessao", "Rotacao", "Grafico FPS",
            "Combo", "Reach", "TNT", "Itens", "Alvo", "Hits", "Abates"};

    // ---------- componentes ----------

    private static class Tog {
        final String label, key;
        final boolean flag, def;
        String desc;
        float x, y, w, h;

        Tog(String label, String key, boolean flag, boolean def) {
            this.label = label;
            this.key = key;
            this.flag = flag;
            this.def = def;
        }

        boolean on() {
            return flag ? Config.getFlag(key, def) : Config.get(key);
        }
    }

    private static class Sl {
        final String key, label;
        final float min, max, step, def;
        final int fmt; // 0 = decimal, 1 = porcentagem, 2 = inteiro
        float x, y, w;

        Sl(String key, String label, float min, float max, float step, float def, int fmt) {
            this.key = key;
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.def = def;
            this.fmt = fmt;
        }

        float value() {
            return Config.getFloat(key, def);
        }

        String text(float v) {
            if (fmt == 1) return Math.round(v * 100F) + "%";
            if (fmt == 2) return String.valueOf(Math.round(v));
            return String.format("%.1f", v);
        }
    }

    private static class Btn {
        final String label;
        final int id;
        final float x, y, w, h;

        Btn(String label, int id, float x, float y, float w, float h) {
            this.label = label;
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private final List<Tog> togs = new ArrayList<Tog>();
    private final List<Sl> sliders = new ArrayList<Sl>();
    private final List<Btn> btns = new ArrayList<Btn>();
    private final Map<String, Float> anim = new HashMap<String, Float>();
    private final float[] tabAnim = new float[TABS.length];
    private final float[] btnAnim = new float[3];
    private Sl dragSl = null;
    private int hoverPreset = -1;

    private int tab = 0;

    public ModernGui() {
    }

    public ModernGui(int startTab) {
        if (startTab >= 0 && startTab < TABS.length) tab = startTab;
    }
    private long openTime;
    private boolean blurOn = false;
    private float fit = 1F;

    @Override
    public void func_73866_w_() {
        openTime = System.currentTimeMillis();
        for (int i = 0; i < tabAnim.length; i++) tabAnim[i] = i == tab ? 1F : 0F;
        updateFit();
        build();
        applyBlur();
    }

    @Override
    public void func_146281_b() {
        try {
            if (blurOn && field_146297_k.field_71460_t.func_147702_a()) field_146297_k.field_71460_t.func_181022_b();
        } catch (Throwable ignored) {
        }
        blurOn = false;
        Config.save();
    }

    private void applyBlur() {
        try {
            boolean want = Config.get("Blur") && OpenGlHelper.field_148824_g;
            if (want && !blurOn) {
                if (field_146297_k.field_71460_t.func_147702_a()) field_146297_k.field_71460_t.func_181022_b();
                field_146297_k.field_71460_t.func_175069_a(new ResourceLocation("shaders/post/blur.json"));
                blurOn = true;
            } else if (!want && blurOn) {
                field_146297_k.field_71460_t.func_181022_b();
                blurOn = false;
            }
        } catch (Throwable ignored) {
        }
    }

    // ---------- layout ----------

    private void updateFit() {
        fit = Math.min(1F, Math.min((field_146294_l - 12F) / PW, (field_146295_m - 12F) / PH));
        if (fit < 0.3F) fit = 0.3F;
    }

    /** Converte o mouse da tela para o espaco do painel (que pode estar reduzido em telas pequenas). */
    private float lx(int mx) {
        return (mx - field_146294_l / 2F) / fit + field_146294_l / 2F;
    }

    private float ly(int my) {
        return (my - field_146295_m / 2F) / fit + field_146295_m / 2F;
    }

    private float px() {
        return (field_146294_l - PW) / 2F;
    }

    private float py() {
        return (field_146295_m - PH) / 2F;
    }

    private boolean ready() {
        return System.currentTimeMillis() - openTime >= 180;
    }

    private float[] tabBounds(int i) {
        float y = py() + 70 + i * 28;
        return new float[]{px() + 14, y, px() + 106, y + 24};
    }

    /** Aplica a transparencia escolhida do menu a uma cor de fundo. */
    private static int ga(int argb) {
        int a = (int) (((argb >>> 24) & 255) * Config.guiAlpha());
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private String desc(String n) {
        if (n.equals("Toggle Sprint")) return "Corre sozinho ao andar para frente";
        if (n.equals("Zoom")) return "Segure a tecla C para aproximar";
        if (n.equals("Fullbright")) return "Brilho maximo em lugares escuros";
        if (n.equals("Hotbar Moderna")) return "Hotbar arredondada com seletor animado";
        if (n.equals("Barras de Status")) return "Vida, fome, armadura, ar e XP em barras";
        if (n.equals("Menus Modernos")) return "Tela inicial e menu de pausa novos";
        if (n.equals("Combo")) return "Golpes seguidos sem apanhar";
        if (n.equals("Reach")) return "Distancia do ultimo golpe";
        if (n.equals("TNT")) return "TNT acesa por perto e tempo para explodir";
        if (n.equals("Itens")) return "Flechas, perolas, macas, pocoes e blocos";
        if (n.equals("Flash de Dano")) return "Bordas vermelhas ao tomar dano";
        if (n.equals("Alvo")) return "Vida e distancia do inimigo";
        if (n.equals("Hits")) return "Golpes dados | recebidos";
        if (n.equals("Abates")) return "Abates | mortes (por mundo)";
        if (n.equals("Aviso Vida Baixa")) return "Borda pulsando com 3 coracoes";
        if (n.equals("Toggle Sneak")) return "Tecla Z liga/desliga o agachar";
        if (n.equals("Zoom Scroll")) return "Scroll muda a forca do zoom";
        if (n.equals("Notificacoes")) return "Avisos rapidos no topo da tela";
        return null;
    }

    private void addToggles(String[] keys, int cols, float rowH, float x0, float y0, float cw) {
        float gap = 5;
        float colW = (cw - gap * (cols - 1)) / cols;
        for (int i = 0; i < keys.length; i++) {
            Tog t = new Tog(keys[i], keys[i], false, false);
            t.desc = desc(keys[i]);
            t.x = x0 + (i % cols) * (colW + gap);
            t.y = y0 + (i / cols) * (rowH + gap);
            t.w = colW;
            t.h = rowH;
            togs.add(t);
        }
    }

    private void addSlider(String key, String label, float min, float max, float step, float def, int fmt,
                           float x, float y, float w) {
        Sl s = new Sl(key, label, min, max, step, def, fmt);
        s.x = x;
        s.y = y;
        s.w = w;
        sliders.add(s);
    }

    private void build() {
        togs.clear();
        sliders.clear();
        btns.clear();
        float x0 = px() + 126;
        float y0 = py() + 54;
        float cw = PW - 126 - 16;
        float right = px() + PW - 16;

        switch (tab) {
            case 0:
                addToggles(HUD_ITEMS, 2, 26, x0, y0, cw);
                break;
            case 1:
                addToggles(COMBAT_ITEMS, 2, 36, x0, y0, cw);
                break;
            case 2:
                buildCrosshair(x0, y0, cw);
                break;
            case 3:
                addToggles(UTIL_ITEMS, 1, 36, x0, y0, cw);
                break;
            case 4:
                addToggles(IFACE_ITEMS, 1, 36, x0, y0, cw);
                break;
            case 5:
                buildLook(x0, y0, cw);
                break;
            default:
                buildSizes(x0, y0, cw);
                break;
        }

        float by = py() + 14;
        if (tab == 0 || tab == 1) {
            btns.add(new Btn("Editar posicoes", 0, right - 112, by, 112, 22));
        } else if (tab == 2) {
            btns.add(new Btn("Resetar mira", 1, right - 112, by, 112, 22));
        } else if (tab == 6) {
            btns.add(new Btn("Resetar tamanhos", 2, right - 118, by, 118, 22));
            btns.add(new Btn("Editar posicoes", 0, right - 118 - 6 - 112, by, 112, 22));
        }
    }

    // ----- aba Mira -----

    private float gridX() {
        return px() + 126 + 120;
    }

    private float gridY() {
        return py() + 54 + 14;
    }

    private float[] presetBounds(int i) {
        float x = gridX() + (i % 6) * 44;
        float y = gridY() + (i / 6) * 44;
        return new float[]{x, y, x + 40, y + 40};
    }

    private float swatchX(int i) {
        return gridX() + 8 + i * 26;
    }

    private float swatchY() {
        return py() + 54 + 224;
    }

    private void buildCrosshair(float x0, float y0, float cw) {
        Tog a = new Tog("Ativar mira", "Mira Moderna", false, false);
        Tog b = new Tog("Contorno", "xh.outline", true, true);
        Tog c = new Tog("Ponto central", "xh.dot", true, false);
        Tog d = new Tog("Dinamica", "xh.dynamic", true, false);
        Tog[] arr = {a, b, c, d};
        for (int i = 0; i < arr.length; i++) {
            arr[i].x = x0;
            arr[i].y = y0 + 114 + i * 24;
            arr[i].w = 108;
            arr[i].h = 20;
            togs.add(arr[i]);
        }
        float gx = gridX();
        float gw = cw - 120;
        float colW = (gw - 10) / 2F;
        float sy = y0 + 154;
        addSlider("xh.size", "Tamanho", 2, 20, 1, 5, 2, gx, sy, colW);
        addSlider("xh.thick", "Espessura", 0.5F, 4, 0.25F, 1, 0, gx + colW + 10, sy, colW);
        addSlider("xh.gap", "Espaco", 0, 12, 0.5F, 3, 0, gx, sy + 27, colW);
        addSlider("xh.alpha", "Opacidade", 0.1F, 1, 0.05F, 0.9F, 1, gx + colW + 10, sy + 27, colW);
        float rw = (gw - 16) / 3F;
        float ry = y0 + 240;
        addSlider("xh.r", "Vermelho", 0, 255, 1, 255, 2, gx, ry, rw);
        addSlider("xh.g", "Verde", 0, 255, 1, 255, 2, gx + rw + 8, ry, rw);
        addSlider("xh.b", "Azul", 0, 255, 1, 255, 2, gx + (rw + 8) * 2, ry, rw);
    }

    // ----- aba Aparencia -----

    private float lookSwatchX(int i) {
        return px() + 126 + 10 + i * 26;
    }

    private float lookSwatchY() {
        return py() + 54 + 92;
    }

    private void buildLook(float x0, float y0, float cw) {
        addToggles(new String[]{"Fundo HUD", "Sombra", "Fonte Moderna", "Blur"}, 2, 26, x0, y0, cw);
        float colW = (cw - 10) / 2F;
        addSlider("opt.bg", "Opacidade do fundo", 0, 1, 0.05F, 0.55F, 1, x0, y0 + 124, colW);
        addSlider("opt.radius", "Arredondamento", 0, 8, 0.5F, 4, 0, x0 + colW + 10, y0 + 124, colW);
        addSlider("opt.guiAlpha", "Transparencia do menu", 0.25F, 1, 0.05F, 0.72F, 1, x0, y0 + 152, colW);
    }

    // ----- aba Tamanho -----

    private void buildSizes(float x0, float y0, float cw) {
        float colW = (cw - 24) / 3F;
        int n = SIZE_ITEMS.length + 1;
        for (int i = 0; i < n; i++) {
            String name = i == 0 ? "Geral" : SIZE_ITEMS[i - 1];
            String key = i == 0 ? "scale.__all" : "scale." + name;
            float x = x0 + (i % 3) * (colW + 12);
            float y = y0 + (i / 3) * 27;
            addSlider(key, name, 0.5F, 2F, 0.05F, 1F, 1, x, y, colW);
        }
    }

    // ---------- desenho ----------

    @Override
    public void func_73863_a(int mx, int my, float partialTicks) {
        float t = Math.min(1F, (System.currentTimeMillis() - openTime) / 180F);
        float e = 1F - (1F - t) * (1F - t) * (1F - t);
        boolean ready = t >= 1F;
        int accent = Config.accent();
        float mxf = lx(mx);
        float myf = ly(my);

        RenderUtil.gradient(0, 0, field_146294_l, field_146295_m,
                RenderUtil.withAlpha(Theme.cur().menuTop, (int) (150 * e)), RenderUtil.withAlpha(Theme.cur().menuBottom, (int) (100 * e)));

        GlStateManager.func_179094_E();
        float sc = (0.94F + 0.06F * e) * fit;
        GlStateManager.func_179109_b(field_146294_l / 2F, field_146295_m / 2F, 0F);
        GlStateManager.func_179152_a(sc, sc, 1F);
        GlStateManager.func_179109_b(-field_146294_l / 2F, -field_146295_m / 2F, 0F);

        float px = px();
        float py = py();

        // painel com borda fina
        RenderUtil.roundedRect(px - 1, py - 1, px + PW + 1, py + PH + 1, 11, Theme.cur().line);
        RenderUtil.roundedRect(px, py, px + PW, py + PH, 10, ga(Theme.cur().panel));
        RenderUtil.roundedRect(px + 8, py + 8, px + 112, py + PH - 8, 8, ga(Theme.cur().side));

        Fonts.drawC("Yani", px + 20, py + 26, accent, 1.35F, true, false);
        Fonts.drawC("Client", px + 20, py + 40, 0xFFFFFFFF, 1.35F, true, false);
        Fonts.drawC("v1.0", px + 20, py + 55, 0xFF6F6F7C, 0.75F, false, false);

        // abas
        boolean lightAccent = Ui.isLight(accent);
        for (int i = 0; i < TABS.length; i++) {
            float[] b = tabBounds(i);
            boolean hov = ready && RenderUtil.hover(mxf, myf, b[0], b[1], b[2], b[3]);
            tabAnim[i] += ((i == tab ? 1F : 0F) - tabAnim[i]) * 0.25F;
            int bg = RenderUtil.lerpColor(hov ? 0x22FFFFFF : 0x00FFFFFF, RenderUtil.withAlpha(accent, 56), tabAnim[i]);
            RenderUtil.roundedRect(b[0], b[1], b[2], b[3], 6, bg);
            if (tabAnim[i] > 0.05F) {
                RenderUtil.roundedRect(b[0] + 2, b[1] + 6, b[0] + 4.5F, b[3] - 6, 1.2F,
                        RenderUtil.withAlpha(accent, (int) (255 * tabAnim[i])));
            }
            int tc = RenderUtil.lerpColor(0xFF8E8E9C, 0xFFFFFFFF, tabAnim[i]);
            Fonts.drawC(TABS[i], b[0] + 14, (b[1] + b[3]) / 2F, tc, 0.95F, tabAnim[i] > 0.5F, false);
        }
        Fonts.drawC("Right Shift fecha",, px + 20, py + PH - 22, 0xFF5E5E6A, 0.72F, false, false);

        // cabecalho
        float x0 = px + 126;
        Fonts.drawC(TABS[tab], x0, py + 24, 0xFFFFFFFF, 1.3F, true, false);
        Fonts.drawC(subtitle(), x0, py + 38, 0xFF6F6F7C, 0.78F, false, false);

        for (Btn b : btns) {
            boolean hov = ready && RenderUtil.hover(mxf, myf, b.x, b.y, b.x + b.w, b.y + b.h);
            btnAnim[b.id] = Ui.approach(btnAnim[b.id], hov);
            Ui.button(b.label, b.x, b.y, b.w, b.h, btnAnim[b.id], b.id == 0);
        }

        if (tab == 2) drawCrosshairPanel(mxf, myf, ready);
        if (tab == 5) drawLookExtras(mxf, myf);

        for (Tog tg : togs) drawToggle(tg, mxf, myf, ready, accent);
        for (Sl s : sliders) {
            boolean hov = ready && RenderUtil.hover(mxf, myf, s.x - 2, s.y, s.x + s.w + 2, s.y + 24);
            drawSlider(s, hov, accent);
        }

        GlStateManager.func_179121_F();
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    private String subtitle() {
        switch (tab) {
            case 0:
                return count(HUD_ITEMS) + " de " + HUD_ITEMS.length + " ativos";
            case 1:
                return count(COMBAT_ITEMS) + " de " + COMBAT_ITEMS.length + " ativos";
            case 2:
                return "Escolha um estilo e personalize";
            case 3:
                return count(UTIL_ITEMS) + " de " + UTIL_ITEMS.length + " ativos";
            case 4:
                return count(IFACE_ITEMS) + " de " + IFACE_ITEMS.length + " ativos";
            case 5:
                return "Temas, cores e transparencia do menu";
            default:
                return "Ajuste a escala de cada elemento";
        }
    }

    private int count(String[] list) {
        int n = 0;
        for (String s : list) if (Config.get(s)) n++;
        return n;
    }

    private void drawToggle(Tog t, float mx, float my, boolean ready, int accent) {
        boolean hov = ready && RenderUtil.hover(mx, my, t.x, t.y, t.x + t.w, t.y + t.h);
        RenderUtil.roundedRect(t.x, t.y, t.x + t.w, t.y + t.h, 6, ga(hov ? Theme.cur().cardHover : Theme.cur().card));

        float cy = t.y + t.h / 2F;
        boolean small = t.h < 24;
        if (t.desc != null) {
            Fonts.drawC(t.label, t.x + 10, t.y + 11, 0xFFFFFFFF, 1F, true, false);
            Fonts.drawC(t.desc, t.x + 10, t.y + 24, 0xFF7A7A88, 0.78F, false, false);
        } else {
            Fonts.drawC(t.label, t.x + 10, cy, 0xFFE8E8F0, small ? 0.9F : 1F, false, false);
        }

        Float cur = anim.get(t.key);
        float a = cur == null ? (t.on() ? 1F : 0F) : cur;
        a += ((t.on() ? 1F : 0F) - a) * 0.25F;
        anim.put(t.key, a);

        float tw = small ? 24F : 28F;
        float th = small ? 12F : 14F;
        float tx = t.x + t.w - 10 - tw;
        float ty = cy - th / 2F;
        RenderUtil.roundedRect(tx, ty, tx + tw, ty + th, th / 2F, RenderUtil.lerpColor(Theme.cur().off, accent, a));
        int knob = Ui.isLight(accent) && a > 0.5F ? 0xFF111114 : 0xFFFFFFFF;
        RenderUtil.circle(tx + th / 2F + a * (tw - th), cy, th / 2F - 2F, knob);
    }

    private void drawSlider(Sl s, boolean hov, int accent) {
        float v = s.value();
        float t = Math.max(0F, Math.min(1F, (v - s.min) / (s.max - s.min)));
        Fonts.drawC(s.label, s.x, s.y + 6, 0xFFE8E8F0, 0.88F, false, false);
        String vt = s.text(v);
        float tw = Fonts.width(vt, 0.85F, true);
        Fonts.drawC(vt, s.x + s.w - tw, s.y + 6, 0xFF9A9AA8, 0.85F, true, false);
        float by = s.y + 18;
        RenderUtil.roundedRect(s.x, by - 2, s.x + s.w, by + 2, 2, ga(Theme.cur().track));
        if (t > 0.001F) RenderUtil.roundedRect(s.x, by - 2, s.x + Math.max(4F, s.w * t), by + 2, 2, accent);
        RenderUtil.circle(s.x + s.w * t, by, (hov || s == dragSl) ? 5.5F : 4.5F, 0xFFFFFFFF);
    }

    private void drawCrosshairPanel(float mx, float my, boolean ready) {
        float x0 = px() + 126;
        float y0 = py() + 54;
        int accent = Config.accent();

        // previa
        RenderUtil.roundedRect(x0, y0, x0 + 108, y0 + 108, 8, ga(Theme.cur().side));
        for (int i = 1; i < 9; i++) {
            RenderUtil.rect(x0 + i * 12, y0 + 4, x0 + i * 12 + 0.5F, y0 + 104, 0x0EFFFFFF);
            RenderUtil.rect(x0 + 4, y0 + i * 12, x0 + 104, y0 + i * 12 + 0.5F, 0x0EFFFFFF);
        }
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(x0 + 54, y0 + 54, 0F);
        GlStateManager.func_179152_a(1.6F, 1.6F, 1F);
        Crosshair.renderPreview(0F, 0F);
        GlStateManager.func_179121_F();
        Fonts.drawC("Previa", x0 + 8, y0 + 100, 0xFF55555F, 0.7F, false, false);

        // estilos
        hoverPreset = -1;
        for (int i = 0; i < Crosshair.STYLES.length; i++) {
            float[] b = presetBounds(i);
            boolean sel = i == Crosshair.style();
            boolean hov = ready && RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3]);
            if (hov) hoverPreset = i;
            if (sel) RenderUtil.roundedRect(b[0] - 1, b[1] - 1, b[2] + 1, b[3] + 1, 7, accent);
            RenderUtil.roundedRect(b[0], b[1], b[2], b[3], 6, ga(hov ? Theme.cur().cardHover : Theme.cur().card));
            Crosshair.draw((b[0] + b[2]) / 2F, (b[1] + b[3]) / 2F, i, 5F, 1F, 2.5F, 0xFFFFFF, sel ? 1F : 0.75F, false, false);
        }
        int shown = hoverPreset >= 0 ? hoverPreset : Crosshair.style();
        float gx = gridX();
        Fonts.drawC("Estilo", gx, y0 + 5, 0xFFE8E8F0, 0.9F, true, false);
        Fonts.drawC(Crosshair.STYLES[shown], gx + 44, y0 + 5, accent, 0.9F, false, false);

        // cores rapidas
        int cur = Crosshair.rgb();
        for (int i = 0; i < Crosshair.PALETTE.length; i++) {
            float sx = swatchX(i);
            float sy = swatchY();
            int col = 0xFF000000 | Crosshair.PALETTE[i];
            if (Crosshair.PALETTE[i] == cur) RenderUtil.circle(sx, sy, 10.5F, 0xFFFFFFFF);
            else if (Crosshair.PALETTE[i] == 0x000000) RenderUtil.circle(sx, sy, 9.5F, 0xFF3A3A46);
            RenderUtil.circle(sx, sy, 8.5F, col);
        }
    }

    private void drawLookExtras(float mx, float my) {
        float x0 = px() + 126;
        float y0 = py() + 54;
        Fonts.drawC("Cor de destaque", x0, y0 + 70, 0xFFE8E8F0, 0.95F, true, false);
        for (int i = 0; i < Config.ACCENTS.length; i++) {
            float sx = lookSwatchX(i);
            float sy = lookSwatchY();
            boolean sel = i == Config.accentIndex();
            if (sel) RenderUtil.circle(sx, sy, 11, 0xFFFFFFFF);
            RenderUtil.circle(sx, sy, sel ? 8.5F : 9F, Config.ACCENTS[i]);
        }

        // fundo da tela inicial (A / B)
        Fonts.drawC("Tema da interface", x0, y0 + 194, 0xFFE8E8F0, 0.95F, true, false);
        for (int i = 0; i < Theme.ALL.length; i++) {
            float[] b = bgCard(i);
            boolean sel = i == Theme.index();
            boolean hov = RenderUtil.hover(mx, my, b[0], b[1], b[2], b[3]);
            if (sel) RenderUtil.roundedRect(b[0] - 1.5F, b[1] - 1.5F, b[2] + 1.5F, b[3] + 1.5F, 7, Config.accent());
            else if (hov) RenderUtil.roundedRect(b[0] - 1, b[1] - 1, b[2] + 1, b[3] + 1, 7, 0x66FFFFFF);
            RenderUtil.roundedRect(b[0], b[1], b[2], b[3], 6, ga(Theme.cur().card));
            MenuBg.thumb(i, b[0] + 3, b[1] + 3, b[2] - b[0] - 6, 36, Theme.ALL[i].accent);
            Fonts.drawC(Theme.ALL[i].name, b[0] + 8, b[3] - 9, sel ? 0xFFFFFFFF : 0xFFB4B4BE, 0.85F, sel, false);
        }
    }

    private float[] bgCard(int i) {
        float x = px() + 126 + i * 134;
        float y = py() + 54 + 206;
        return new float[]{x, y, x + 126, y + 58};
    }

    // ---------- entrada ----------

    private void setSlider(Sl s, float mxf) {
        float t = Math.max(0F, Math.min(1F, (mxf - s.x) / s.w));
        float v = s.min + t * (s.max - s.min);
        v = Math.round(v / s.step) * s.step;
        v = Math.max(s.min, Math.min(s.max, v));
        Config.setFloat(s.key, v);
    }

    @Override
    protected void func_73864_a(int mx, int my, int btn) throws IOException {
        super.func_73864_a(mx, my, btn);
        if (btn != 0 || !ready()) return;
        float x = lx(mx);
        float y = ly(my);

        for (int i = 0; i < TABS.length; i++) {
            float[] b = tabBounds(i);
            if (RenderUtil.hover(x, y, b[0], b[1], b[2], b[3])) {
                tab = i;
                dragSl = null;
                build();
                return;
            }
        }

        for (Btn b : btns) {
            if (!RenderUtil.hover(x, y, b.x, b.y, b.x + b.w, b.y + b.h)) continue;
            if (b.id == 0) {
                field_146297_k.func_147108_a(new HudEditorGui(this));
            } else if (b.id == 1) {
                Crosshair.reset();
                Config.save();
            } else {
                Config.removePrefix("scale.");
                Config.save();
            }
            return;
        }

        for (Tog t : togs) {
            if (!RenderUtil.hover(x, y, t.x, t.y, t.x + t.w, t.y + t.h)) continue;
            if (t.flag) Config.setFlag(t.key, !Config.getFlag(t.key, t.def));
            else Config.toggle(t.key);
            Config.save();
            if (t.key.equals("Blur")) applyBlur();
            return;
        }

        for (Sl s : sliders) {
            if (RenderUtil.hover(x, y, s.x - 4, s.y, s.x + s.w + 4, s.y + 24)) {
                dragSl = s;
                setSlider(s, x);
                return;
            }
        }

        if (tab == 2) {
            for (int i = 0; i < Crosshair.STYLES.length; i++) {
                float[] b = presetBounds(i);
                if (RenderUtil.hover(x, y, b[0], b[1], b[2], b[3])) {
                    Config.setInt("xh.style", i);
                    Config.save();
                    return;
                }
            }
            for (int i = 0; i < Crosshair.PALETTE.length; i++) {
                float dx = x - swatchX(i);
                float dy = y - swatchY();
                if (dx * dx + dy * dy <= 11 * 11) {
                    Crosshair.setRgb(Crosshair.PALETTE[i]);
                    Config.save();
                    return;
                }
            }
        }

        if (tab == 5) {
            for (int i = 0; i < Theme.ALL.length; i++) {
                float[] b = bgCard(i);
                if (RenderUtil.hover(x, y, b[0], b[1], b[2], b[3])) {
                    Theme.set(i);
                    return;
                }
            }
            for (int i = 0; i < Config.ACCENTS.length; i++) {
                float dx = x - lookSwatchX(i);
                float dy = y - lookSwatchY();
                if (dx * dx + dy * dy <= 12 * 12) {
                    Config.setAccent(i);
                    Config.save();
                    return;
                }
            }
        }
    }

    @Override
    protected void func_146273_a(int mx, int my, int btn, long time) {
        if (dragSl != null) setSlider(dragSl, lx(mx));
    }

    @Override
    protected void func_146286_b(int mx, int my, int state) {
        super.func_146286_b(mx, my, state);
        if (dragSl != null) {
            dragSl = null;
            Config.save();
        }
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RSHIFT) {
            field_146297_k.func_147108_a(null);
            return;
        }
        super.func_73869_a(typedChar, keyCode);
    }

    @Override
    public boolean func_73868_f() {
        return false;
    }
}
