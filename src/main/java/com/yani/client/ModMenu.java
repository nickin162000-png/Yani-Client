package com.yani.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Menu principal do Yani Client (tecla Right Shift): grade de mods com
 * OPTIONS / ATIVADO-DESATIVADO, filtros, busca, perfis e editor de HUD.
 */
public class ModMenu extends GuiScreen {

    private static final int PW = 560;
    private static final int PH = 344;

    private static final String[] TOP_TABS = {"MODS", "MIRA", "TAMANHO", "AJUSTES"};
    private static final String[] FILTERS = {"TODOS", "NOVOS", "HUD", "COMBATE", "UTILIDADES", "INTERFACE"};

    private static final int CARD_H = 118;
    private static final int GAP = 10;
    private static final int MAX_USER_PROFILES = 6;

    private static final int GREEN = 0xFF22A55E;
    private static final int GREEN_HOVER = 0xFF2BC070;
    private static final int RED = 0xFFB3243F;
    private static final int RED_HOVER = 0xFFCC2E4C;

    // ---------- lista de mods ----------

    private static class Mod {
        final String key, cat, desc;
        final boolean isNew;
        final int optionsTab;

        Mod(String key, String cat, int optionsTab, boolean isNew, String desc) {
            this.key = key;
            this.cat = cat;
            this.optionsTab = optionsTab;
            this.isNew = isNew;
            this.desc = desc;
        }
    }

    private static final List<Mod> MODS = new ArrayList<Mod>();

    static {
        String[] hud = {"Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio", "Keystrokes",
                "Teclas Mouse", "Armor HUD", "Efeitos", "Velocidade", "Memoria", "Data", "Sessao", "Rotacao", "Grafico FPS"};
        String[] combat = {"Combo", "Reach", "TNT", "Itens", "Flash de Dano", "Alvo", "Hits", "Abates", "Aviso Vida Baixa"};
        String[] util = {"Toggle Sprint", "Zoom", "Fullbright", "Toggle Sneak", "Zoom Scroll", "Notificacoes"};
        String[] iface = {"Hotbar Moderna", "Barras de Status", "Mira Moderna", "Menus Modernos"};
        Set<String> fresh = new HashSet<String>(Arrays.asList("Rotacao", "Grafico FPS", "Zoom Scroll", "Sessao"));
        for (String k : hud) MODS.add(new Mod(k, "HUD", 0, fresh.contains(k), null));
        for (String k : combat) MODS.add(new Mod(k, "COMBATE", 1, fresh.contains(k), null));
        for (String k : util) MODS.add(new Mod(k, "UTILIDADES", 3, fresh.contains(k), null));
        for (String k : iface) MODS.add(new Mod(k, "INTERFACE", k.equals("Mira Moderna") ? 2 : 4, fresh.contains(k), null));
    }

    // ---------- perfis ----------

    private static final String[] BUILTIN_NAMES = {"Padrao", "UHC", "Arena PvP"};
    private static final Map<String, Set<String>> BUILTIN = new HashMap<String, Set<String>>();

    static {
        BUILTIN.put("UHC", new HashSet<String>(Arrays.asList("Marca", "FPS", "CPS", "Relogio", "Coordenadas", "Direcao",
                "Armor HUD", "Efeitos", "Itens", "Alvo", "Aviso Vida Baixa", "Toggle Sprint", "Zoom", "Notificacoes",
                "Hotbar Moderna", "Barras de Status", "Mira Moderna", "Menus Modernos")));
        BUILTIN.put("Arena PvP", new HashSet<String>(Arrays.asList("Marca", "FPS", "CPS", "Ping", "Keystrokes",
                "Teclas Mouse", "Armor HUD", "Combo", "Reach", "Alvo", "Hits", "Abates", "Flash de Dano", "Itens",
                "Aviso Vida Baixa", "Toggle Sprint", "Notificacoes", "Hotbar Moderna", "Barras de Status",
                "Mira Moderna", "Menus Modernos")));
    }

    // ---------- estado ----------

    private final Map<String, Float> cardAnim = new HashMap<String, Float>();
    private final float[] tabAnim = new float[TOP_TABS.length];
    private final float[] filterAnim = new float[FILTERS.length];
    private final float[] profAnim = new float[BUILTIN_NAMES.length + MAX_USER_PROFILES];
    private final float[] btnAnim = new float[2];

    private int filter = 0;
    private String query = "";
    private boolean searchFocus = false;
    private float scroll = 0F;
    private float scrollGoal = 0F;
    private long openTime;
    private float fit = 1F;

    @Override
    public void func_73866_w_() {
        openTime = System.currentTimeMillis();
        updateFit();
    }

    @Override
    public void func_146281_b() {
        Config.save();
    }

    // ---------- layout ----------

    private void updateFit() {
        fit = Math.min(1F, Math.min((field_146294_l - 12F) / PW, (field_146295_m - 12F) / PH));
        if (fit < 0.3F) fit = 0.3F;
    }

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

    private float areaX() {
        return px() + 138;
    }

    private float areaW() {
        return PW - 138 - 10;
    }

    private float gridTop() {
        return py() + 84;
    }

    private float gridBottom() {
        return py() + PH - 8;
    }

    private float cardW() {
        return (areaW() - GAP * 2) / 3F;
    }

    private float[] tabBounds(int i) {
        float x = px() + 226 + i * 78;
        return new float[]{x, py() + 14, x + 70, py() + 34};
    }

    private float[] closeBounds() {
        return new float[]{px() + PW - 30, py() + 14, px() + PW - 10, py() + 34};
    }

    private float[] searchBounds() {
        float w = 116;
        return new float[]{areaX() + areaW() - w, py() + 58, areaX() + areaW(), py() + 78};
    }

    private float[] filterBounds(int i) {
        float x = areaX();
        for (int k = 0; k < i; k++) x += Fonts.width(FILTERS[k], 0.8F, true) + 22 + 6;
        float w = Fonts.width(FILTERS[i], 0.8F, true) + 22;
        return new float[]{x, py() + 58, x + w, py() + 78};
    }

    private float[] profileBounds(int i) {
        float y = py() + 68 + i * 23;
        return new float[]{px() + 14, y, px() + 122, y + 20};
    }

    private float[] saveBounds() {
        return new float[]{px() + 14, py() + PH - 62, px() + 122, py() + PH - 40};
    }

    private float[] hudBounds() {
        return new float[]{px() + 14, py() + PH - 34, px() + 122, py() + PH - 12};
    }

    // ---------- dados ----------

    private List<Mod> visible() {
        List<Mod> out = new ArrayList<Mod>();
        String q = query.toLowerCase();
        for (Mod m : MODS) {
            if (filter == 1 && !m.isNew) continue;
            if (filter == 2 && !m.cat.equals("HUD")) continue;
            if (filter == 3 && !m.cat.equals("COMBATE")) continue;
            if (filter == 4 && !m.cat.equals("UTILIDADES")) continue;
            if (filter == 5 && !m.cat.equals("INTERFACE")) continue;
            if (q.length() > 0 && !m.key.toLowerCase().contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    private float maxScroll(int count) {
        int rows = (count + 2) / 3;
        float content = rows * (CARD_H + GAP) - GAP;
        return Math.max(0F, content - (gridBottom() - gridTop()));
    }

    private static String initials(String key) {
        String[] p = key.split(" ");
        if (p.length >= 2) return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
        return key.length() >= 2 ? key.substring(0, 2).toUpperCase() : key.toUpperCase();
    }

    // ---------- perfis: salvar / carregar ----------

    private static int userProfileCount() {
        return Math.max(0, Math.min(MAX_USER_PROFILES, Config.getInt("profile.count", 0)));
    }

    private static String profileName(int i) {
        if (i < BUILTIN_NAMES.length) return BUILTIN_NAMES[i];
        return Config.getString("profile." + (i - BUILTIN_NAMES.length) + ".name", "Perfil");
    }

    private static boolean isModKey(String n) {
        for (Mod m : MODS) if (m.key.equals(n)) return true;
        return false;
    }

    private static void applyProfile(int i) {
        String name = profileName(i);
        if (i == 0) {
            for (Mod m : MODS) Config.setEnabled(m.key, Config.isDefaultOn(m.key));
        } else if (i < BUILTIN_NAMES.length) {
            Set<String> on = BUILTIN.get(name);
            for (Mod m : MODS) Config.setEnabled(m.key, on != null && on.contains(m.key));
        } else {
            String data = Config.getString("profile." + (i - BUILTIN_NAMES.length) + ".data", "");
            for (int k = 0; k < Config.NAMES.length && k < data.length(); k++) {
                if (isModKey(Config.NAMES[k])) Config.setEnabled(Config.NAMES[k], data.charAt(k) == '1');
            }
        }
        Config.setString("profile.active", name);
        Config.save();
        Toast.show("Perfil: " + name);
    }

    private static void saveProfile() {
        int n = userProfileCount();
        if (n >= MAX_USER_PROFILES) {
            Toast.show("Limite de perfis atingido");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String name : Config.NAMES) sb.append(Config.get(name) ? '1' : '0');
        String name = "Perfil " + (n + 1);
        Config.setString("profile." + n + ".name", name);
        Config.setString("profile." + n + ".data", sb.toString());
        Config.setInt("profile.count", n + 1);
        Config.setString("profile.active", name);
        Config.save();
        Toast.show("Perfil salvo: " + name);
    }

    private static void deleteProfile(int user) {
        int n = userProfileCount();
        for (int k = user; k < n - 1; k++) {
            Config.setString("profile." + k + ".name", Config.getString("profile." + (k + 1) + ".name", "Perfil"));
            Config.setString("profile." + k + ".data", Config.getString("profile." + (k + 1) + ".data", ""));
        }
        Config.removePrefix("profile." + (n - 1) + ".");
        Config.setInt("profile.count", n - 1);
        Config.save();
    }

    // ---------- desenho ----------

    @Override
    public void func_73863_a(int mx, int my, float partialTicks) {
        float t = Math.min(1F, (System.currentTimeMillis() - openTime) / 160F);
        int accent = Config.accent();
        Theme.T th = Theme.cur();
        float x = lx(mx);
        float y = ly(my);

        RenderUtil.gradient(0, 0, field_146294_l, field_146295_m,
                RenderUtil.withAlpha(th.menuTop, (int) (150 * t)), RenderUtil.withAlpha(th.menuBottom, (int) (100 * t)));

        net.minecraft.client.renderer.GlStateManager.func_179094_E();
        net.minecraft.client.renderer.GlStateManager.func_179109_b(field_146294_l / 2F, field_146295_m / 2F, 0F);
        net.minecraft.client.renderer.GlStateManager.func_179152_a(fit, fit, 1F);
        net.minecraft.client.renderer.GlStateManager.func_179109_b(-field_146294_l / 2F, -field_146295_m / 2F, 0F);

        float px = px();
        float py = py();
        RenderUtil.roundedRect(px - 1, py - 1, px + PW + 1, py + PH + 1, 11, th.line);
        RenderUtil.roundedRect(px, py, px + PW, py + PH, 10, Theme.ga(th.panel));

        drawHeader(x, y, accent, th);
        drawSidebar(x, y, accent, th);
        drawFilters(x, y, accent, th);
        drawGrid(x, y, accent, th);

        net.minecraft.client.renderer.GlStateManager.func_179121_F();
        net.minecraft.client.renderer.GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    private void drawHeader(float x, float y, int accent, Theme.T th) {
        float px = px();
        float py = py();
        Ui.logo(px + 14, py + 9, 30, accent);
        String a = "YANI";
        String b = " CLIENT";
        float wa = Fonts.width(a, 1.3F, true);
        Fonts.drawC(a, px + 50, py + 24, 0xFFFFFFFF, 1.3F, true, false);
        Fonts.drawC(b, px + 50 + wa, py + 24, 0xFFC8CCE0, 1.3F, false, false);

        for (int i = 0; i < TOP_TABS.length; i++) {
            float[] r = tabBounds(i);
            boolean hov = RenderUtil.hover(x, y, r[0], r[1], r[2], r[3]);
            tabAnim[i] = Ui.approach(tabAnim[i], hov || i == 0);
            int bg = i == 0 ? RenderUtil.withAlpha(accent, 0xB4)
                    : RenderUtil.lerpColor(th.btn, th.btnHover, tabAnim[i]);
            RenderUtil.roundedRect(r[0], r[1], r[2], r[3], 5, bg);
            int tc = (i == 0 && Ui.isLight(accent)) ? 0xFF111114 : 0xFFFFFFFF;
            float w = Fonts.width(TOP_TABS[i], 0.85F, true);
            Fonts.drawC(TOP_TABS[i], r[0] + (r[2] - r[0] - w) / 2F, (r[1] + r[3]) / 2F, tc, 0.85F, true, false);
        }

        float[] c = closeBounds();
        boolean hov = RenderUtil.hover(x, y, c[0], c[1], c[2], c[3]);
        RenderUtil.roundedRect(c[0], c[1], c[2], c[3], 5, hov ? 0xFFB3243F : th.btn);
        float cx = (c[0] + c[2]) / 2F;
        float cy = (c[1] + c[3]) / 2F;
        RenderUtil.line(cx - 3.5F, cy - 3.5F, cx + 3.5F, cy + 3.5F, 1.4F, 0xFFFFFFFF);
        RenderUtil.line(cx - 3.5F, cy + 3.5F, cx + 3.5F, cy - 3.5F, 1.4F, 0xFFFFFFFF);

        RenderUtil.rect(px + 12, py + 46, px + PW - 12, py + 46.6F, 0x22FFFFFF);
    }

    private void drawSidebar(float x, float y, int accent, Theme.T th) {
        float px = px();
        float py = py();
        RenderUtil.roundedRect(px + 8, py + 54, px + 130, py + PH - 8, 8, Theme.ga(th.side));
        Fonts.drawC("PERFIS", px + 16, py + 50 + 5, 0xFF6F6F7C, 0.7F, true, false);

        String active = Config.getString("profile.active", "Padrao");
        int total = BUILTIN_NAMES.length + userProfileCount();
        for (int i = 0; i < total; i++) {
            float[] r = profileBounds(i);
            String name = profileName(i);
            boolean sel = name.equals(active);
            boolean hov = RenderUtil.hover(x, y, r[0], r[1], r[2], r[3]);
            profAnim[i] = Ui.approach(profAnim[i], hov || sel);
            int bg = sel ? RenderUtil.withAlpha(accent, 70) : RenderUtil.lerpColor(0x00FFFFFF, 0x22FFFFFF, profAnim[i]);
            RenderUtil.roundedRect(r[0], r[1], r[2], r[3], 5, bg);
            if (sel) RenderUtil.roundedRect(r[0] + 1.5F, r[1] + 5, r[0] + 3.5F, r[3] - 5, 1F, accent);
            Fonts.drawC(name, r[0] + 10, (r[1] + r[3]) / 2F, sel ? 0xFFFFFFFF : 0xFFB4B4C0, 0.85F, sel, false);
            if (i >= BUILTIN_NAMES.length) {
                float cx = r[2] - 10;
                float cy = (r[1] + r[3]) / 2F;
                int xc = RenderUtil.hover(x, y, cx - 6, r[1], r[2], r[3]) ? 0xFFFF6B7D : 0xFF7A7A88;
                RenderUtil.line(cx - 2.5F, cy - 2.5F, cx + 2.5F, cy + 2.5F, 1.2F, xc);
                RenderUtil.line(cx - 2.5F, cy + 2.5F, cx + 2.5F, cy - 2.5F, 1.2F, xc);
            }
        }

        float[] s = saveBounds();
        btnAnim[0] = Ui.approach(btnAnim[0], RenderUtil.hover(x, y, s[0], s[1], s[2], s[3]));
        Ui.button("SALVAR PERFIL", s[0], s[1], s[2] - s[0], s[3] - s[1], btnAnim[0], false);
        float[] h = hudBounds();
        btnAnim[1] = Ui.approach(btnAnim[1], RenderUtil.hover(x, y, h[0], h[1], h[2], h[3]));
        Ui.button("EDITAR HUD", h[0], h[1], h[2] - h[0], h[3] - h[1], btnAnim[1], true);
    }

    private void drawFilters(float x, float y, int accent, Theme.T th) {
        for (int i = 0; i < FILTERS.length; i++) {
            float[] r = filterBounds(i);
            boolean hov = RenderUtil.hover(x, y, r[0], r[1], r[2], r[3]);
            filterAnim[i] = Ui.approach(filterAnim[i], hov || i == filter);
            int bg = i == filter ? accent : RenderUtil.lerpColor(th.btn, th.btnHover, filterAnim[i]);
            RenderUtil.roundedRect(r[0], r[1], r[2], r[3], 5, bg);
            int tc = (i == filter && Ui.isLight(accent)) ? 0xFF111114 : 0xFFFFFFFF;
            Fonts.drawC(FILTERS[i], r[0] + 11, (r[1] + r[3]) / 2F, tc, 0.8F, true, false);
        }

        float[] s = searchBounds();
        RenderUtil.roundedRect(s[0] - 1, s[1] - 1, s[2] + 1, s[3] + 1, 6, searchFocus ? accent : th.line);
        RenderUtil.roundedRect(s[0], s[1], s[2], s[3], 5, 0xFF111118);
        // lupa
        RenderUtil.ring(s[0] + 10, (s[1] + s[3]) / 2F - 1, 3.2F, 1.1F, 0xFF8A8A98);
        RenderUtil.line(s[0] + 12.5F, (s[1] + s[3]) / 2F + 1.5F, s[0] + 15, (s[1] + s[3]) / 2F + 4, 1.2F, 0xFF8A8A98);
        boolean blink = (System.currentTimeMillis() / 500L) % 2L == 0L;
        String shown = query.length() == 0 && !searchFocus ? "Buscar mods" : query + (searchFocus && blink ? "|" : "");
        int col = query.length() == 0 && !searchFocus ? 0xFF6F6F7C : 0xFFE8E8F0;
        Fonts.drawC(shown, s[0] + 22, (s[1] + s[3]) / 2F, col, 0.85F, false, false);
    }

    private void drawGrid(float x, float y, int accent, Theme.T th) {
        List<Mod> list = visible();
        float max = maxScroll(list.size());
        scrollGoal = Math.max(0F, Math.min(max, scrollGoal));
        scroll += (scrollGoal - scroll) * 0.3F;
        if (Math.abs(scrollGoal - scroll) < 0.05F) scroll = scrollGoal;

        float gx = areaX();
        float gt = gridTop();
        float gb = gridBottom();
        float cw = cardW();

        if (list.isEmpty()) {
            String m = "Nenhum mod encontrado";
            float w = Fonts.width(m, 1F, false);
            Fonts.drawC(m, gx + (areaW() - w) / 2F, gt + 60, 0xFF6F6F7C, 1F, false, false);
            return;
        }

        beginClip(gx - 2, gt, gx + areaW() + 2, gb);
        boolean inView = y >= gt && y <= gb;
        for (int i = 0; i < list.size(); i++) {
            Mod m = list.get(i);
            float cx = gx + (i % 3) * (cw + GAP);
            float cy = gt + (i / 3) * (CARD_H + GAP) - scroll;
            if (cy + CARD_H < gt || cy > gb) continue;
            boolean over = inView && RenderUtil.hover(x, y, cx, cy, cx + cw, cy + CARD_H);
            drawCard(m, cx, cy, cw, x, y, over, accent, th);
        }
        endClip();

        if (max > 0F) {
            float trackH = gb - gt;
            float barH = Math.max(18F, trackH * trackH / (trackH + max));
            float barY = gt + (trackH - barH) * (scroll / max);
            float bx = px() + PW - 7;
            RenderUtil.roundedRect(bx, gt, bx + 2.5F, gb, 1.2F, 0x18FFFFFF);
            RenderUtil.roundedRect(bx, barY, bx + 2.5F, barY + barH, 1.2F, RenderUtil.withAlpha(accent, 0xC0));
        }
    }

    private void drawCard(Mod m, float cx, float cy, float cw, float x, float y, boolean over, int accent, Theme.T th) {
        boolean on = Config.get(m.key);
        Float cur = cardAnim.get(m.key);
        float a = cur == null ? 0F : cur;
        a = Ui.approach(a, over);
        cardAnim.put(m.key, a);

        RenderUtil.roundedRect(cx, cy, cx + cw, cy + CARD_H, 7, RenderUtil.lerpColor(Theme.ga(th.card), Theme.ga(th.cardHover), a));

        // icone (selo com iniciais)
        float ix = cx + cw / 2F;
        float iy = cy + 34;
        int tone = on ? accent : 0xFF6A6A78;
        RenderUtil.circle(ix, iy, 19, RenderUtil.withAlpha(tone, 0x30));
        RenderUtil.ring(ix, iy, 17, 1.6F, RenderUtil.withAlpha(tone, on ? 0xFF : 0xA0));
        String ini = initials(m.key);
        float iw = Fonts.width(ini, 1.25F, true);
        Fonts.drawC(ini, ix - iw / 2F, iy, on ? 0xFFFFFFFF : 0xFF9A9AA8, 1.25F, true, false);

        if (m.isNew) Fonts.drawC("NOVO", cx + cw - 30, cy + 10, 0xFFFF4D5E, 0.62F, true, false);

        float nw = Fonts.width(m.key, 0.95F, false);
        Fonts.drawC(m.key, cx + (cw - nw) / 2F, cy + 70, on ? 0xFFFFFFFF : 0xFFA0A0AE, 0.95F, false, false);

        // OPTIONS + engrenagem
        float oy = cy + CARD_H - 46;
        float gw = 22;
        boolean hovOpt = RenderUtil.hover(x, y, cx, oy, cx + cw - gw - 1, oy + 22) && inGrid(y);
        boolean hovGear = RenderUtil.hover(x, y, cx + cw - gw, oy, cx + cw, oy + 22) && inGrid(y);
        RenderUtil.rect(cx, oy, cx + cw - gw - 1, oy + 22, hovOpt ? 0x30FFFFFF : 0x18FFFFFF);
        RenderUtil.rect(cx + cw - gw, oy, cx + cw, oy + 22, hovGear ? 0x30FFFFFF : 0x18FFFFFF);
        String ot = "OPCOES";
        float ow = Fonts.width(ot, 0.78F, true);
        Fonts.drawC(ot, cx + (cw - gw - ow) / 2F, oy + 11, 0xFFE8E8F0, 0.78F, true, false);
        float gcx = cx + cw - gw / 2F;
        float gcy = oy + 11;
        RenderUtil.ring(gcx, gcy, 4.2F, 1.8F, 0xFFE8E8F0);
        RenderUtil.circle(gcx, gcy, 1.4F, 0xFFE8E8F0);

        // barra ATIVADO / DESATIVADO
        float sy = cy + CARD_H - 22;
        boolean hovBar = RenderUtil.hover(x, y, cx, sy, cx + cw, cy + CARD_H) && inGrid(y);
        int bar = on ? (hovBar ? GREEN_HOVER : GREEN) : (hovBar ? RED_HOVER : RED);
        RenderUtil.roundedRect(cx, sy, cx + cw, cy + CARD_H, 7, bar);
        // deixa a borda de cima da barra reta (so os cantos de baixo ficam arredondados)
        RenderUtil.rect(cx, sy, cx + cw, sy + 7, bar);
        String st = on ? "ATIVADO" : "DESATIVADO";
        float sw = Fonts.width(st, 0.8F, true);
        Fonts.drawC(st, cx + (cw - sw) / 2F, sy + 11, 0xFFFFFFFF, 0.8F, true, false);
    }

    private boolean inGrid(float y) {
        return y >= gridTop() && y <= gridBottom();
    }

    // ---------- recorte (scissor) ----------

    private void beginClip(float x1, float y1, float x2, float y2) {
        float cx = field_146294_l / 2F;
        float cy = field_146295_m / 2F;
        float sx1 = (x1 - cx) * fit + cx;
        float sx2 = (x2 - cx) * fit + cx;
        float sy1 = (y1 - cy) * fit + cy;
        float sy2 = (y2 - cy) * fit + cy;
        int f = new ScaledResolution(field_146297_k).func_78325_e();
        int bx = Math.max(0, (int) (sx1 * f));
        int by = Math.max(0, field_146297_k.field_71440_d - (int) (sy2 * f));
        int bw = Math.max(0, (int) ((sx2 - sx1) * f));
        int bh = Math.max(0, (int) ((sy2 - sy1) * f));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(bx, by, bw, bh);
    }

    private void endClip() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // ---------- entrada ----------

    @Override
    protected void func_73864_a(int mx, int my, int btn) throws IOException {
        super.func_73864_a(mx, my, btn);
        if (btn != 0) return;
        float x = lx(mx);
        float y = ly(my);

        // busca
        float[] s = searchBounds();
        searchFocus = RenderUtil.hover(x, y, s[0], s[1], s[2], s[3]);
        if (searchFocus) return;

        float[] c = closeBounds();
        if (RenderUtil.hover(x, y, c[0], c[1], c[2], c[3])) {
            field_146297_k.func_147108_a(null);
            return;
        }

        for (int i = 1; i < TOP_TABS.length; i++) {
            float[] r = tabBounds(i);
            if (!RenderUtil.hover(x, y, r[0], r[1], r[2], r[3])) continue;
            int target = i == 1 ? 2 : (i == 2 ? 6 : 5);
            field_146297_k.func_147108_a(new ModernGui(target));
            return;
        }

        for (int i = 0; i < FILTERS.length; i++) {
            float[] r = filterBounds(i);
            if (RenderUtil.hover(x, y, r[0], r[1], r[2], r[3])) {
                filter = i;
                scroll = 0F;
                scrollGoal = 0F;
                return;
            }
        }

        // perfis
        int total = BUILTIN_NAMES.length + userProfileCount();
        for (int i = 0; i < total; i++) {
            float[] r = profileBounds(i);
            if (!RenderUtil.hover(x, y, r[0], r[1], r[2], r[3])) continue;
            if (i >= BUILTIN_NAMES.length && x >= r[2] - 16) {
                deleteProfile(i - BUILTIN_NAMES.length);
            } else {
                applyProfile(i);
            }
            return;
        }

        float[] sv = saveBounds();
        if (RenderUtil.hover(x, y, sv[0], sv[1], sv[2], sv[3])) {
            saveProfile();
            return;
        }
        float[] hb = hudBounds();
        if (RenderUtil.hover(x, y, hb[0], hb[1], hb[2], hb[3])) {
            field_146297_k.func_147108_a(new HudEditorGui(this));
            return;
        }

        // cards
        if (!inGrid(y)) return;
        List<Mod> list = visible();
        float cw = cardW();
        for (int i = 0; i < list.size(); i++) {
            Mod m = list.get(i);
            float cx = areaX() + (i % 3) * (cw + GAP);
            float cy = gridTop() + (i / 3) * (CARD_H + GAP) - scroll;
            if (!RenderUtil.hover(x, y, cx, cy, cx + cw, cy + CARD_H)) continue;
            float oy = cy + CARD_H - 46;
            if (y >= oy && y < oy + 22) {
                field_146297_k.func_147108_a(new ModernGui(m.optionsTab));
            } else if (y >= oy + 24) {
                Config.toggle(m.key);
                Config.save();
                Config.setString("profile.active", "");
            }
            return;
        }
    }

    @Override
    public void func_146274_d() throws IOException {
        super.func_146274_d();
        int d = Mouse.getEventDWheel();
        if (d != 0) scrollGoal -= (d > 0 ? 1 : -1) * 46F;
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RSHIFT) {
            field_146297_k.func_147108_a(null);
            return;
        }
        if (searchFocus) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
                searchFocus = false;
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (query.length() > 0) query = query.substring(0, query.length() - 1);
                scrollGoal = 0F;
            } else if (typedChar >= 32 && typedChar != 127 && query.length() < 16) {
                query += typedChar;
                scrollGoal = 0F;
            }
            return;
        }
        super.func_73869_a(typedChar, keyCode);
    }

    @Override
    public boolean func_73868_f() {
        return false;
    }
}
