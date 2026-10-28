package com.yani.client;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Config {
    public static final String[] NAMES = {
            // HUD
            "Marca", "FPS", "CPS", "Ping", "Coordenadas", "Direcao", "Relogio",
            "Keystrokes", "Teclas Mouse", "Armor HUD", "Efeitos", "Velocidade", "Memoria", "Data",
            "Sessao", "Rotacao", "Grafico FPS",
            // Combate
            "Combo", "Reach", "TNT", "Itens", "Flash de Dano",
            "Alvo", "Hits", "Abates", "Aviso Vida Baixa",
            // Utilidades
            "Toggle Sprint", "Zoom", "Fullbright",
            "Toggle Sneak", "Zoom Scroll", "Notificacoes",
            // Interface
            "Hotbar Moderna", "Barras de Status", "Mira Moderna", "Menus Modernos",
            // Aparencia
            "Fundo HUD", "Sombra", "Fonte Moderna", "Blur"
    };

    /** Modulos que comecam desligados na primeira vez. */
    private static final Set<String> OFF_BY_DEFAULT = new HashSet<String>(Arrays.asList(
            "Fullbright", "Velocidade", "Memoria", "Data",
            "Sessao", "Rotacao", "Grafico FPS", "Hits", "Abates", "Toggle Sneak"));

    public static final int[] ACCENTS = {
            0xFF7C5CFF, 0xFF2D9CFF, 0xFF2ED573, 0xFFFF4757, 0xFFFFA502, 0xFFFF6BCB, 0xFF18DCFF, 0xFFF5F5F7
    };

    private static final Map<String, Boolean> MAP = new HashMap<String, Boolean>();
    private static final Map<String, String> EXTRA = new HashMap<String, String>();
    private static final Set<String> RESERVED = new HashSet<String>();
    /** -1 = usa a cor do tema; 0..7 = cor escolhida manualmente. */
    private static int accent = -1;
    private static File file;

    static {
        RESERVED.addAll(Arrays.asList(NAMES));
        RESERVED.add("accent");
        RESERVED.add("accentCustom");
    }

    public static void load() {
        file = new File(Minecraft.func_71410_x().field_71412_D, "config/yaniclient.properties");
        Properties p = new Properties();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        for (String n : NAMES) {
            boolean def = !OFF_BY_DEFAULT.contains(n);
            MAP.put(n, Boolean.parseBoolean(p.getProperty(n, String.valueOf(def))));
        }
        for (String key : p.stringPropertyNames()) {
            if (!RESERVED.contains(key)) EXTRA.put(key, p.getProperty(key));
        }
        try {
            accent = Integer.parseInt(p.getProperty("accentCustom", "-1"));
        } catch (NumberFormatException e) {
            accent = -1;
        }
        if (accent < -1 || accent >= ACCENTS.length) accent = -1;
    }

    public static void save() {
        if (file == null) return;
        Properties p = new Properties();
        for (String n : NAMES) p.setProperty(n, String.valueOf(get(n)));
        p.setProperty("accentCustom", String.valueOf(accent));
        for (Map.Entry<String, String> en : EXTRA.entrySet()) p.setProperty(en.getKey(), en.getValue());
        try {
            file.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(file)) {
                p.store(out, "Yani Client");
            }
        } catch (IOException ignored) {
        }
    }

    // ---------- opcoes ligar/desligar ----------

    public static boolean get(String name) {
        Boolean b = MAP.get(name);
        return b != null && b;
    }

    public static void toggle(String name) {
        MAP.put(name, !get(name));
    }

    // ---------- valores extras (posicao, escala, mira...) ----------

    public static float getFloat(String key, float def) {
        String v = EXTRA.get(key);
        if (v == null) return def;
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void setFloat(String key, float value) {
        EXTRA.put(key, String.valueOf(value));
    }

    public static int getInt(String key, int def) {
        String v = EXTRA.get(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void setInt(String key, int value) {
        EXTRA.put(key, String.valueOf(value));
    }

    public static boolean getFlag(String key, boolean def) {
        String v = EXTRA.get(key);
        if (v == null) return def;
        return v.equals("1");
    }

    public static void setFlag(String key, boolean value) {
        EXTRA.put(key, value ? "1" : "0");
    }

    public static String getString(String key, String def) {
        String v = EXTRA.get(key);
        return v == null ? def : v;
    }

    public static void setString(String key, String value) {
        EXTRA.put(key, value);
    }

    public static void setEnabled(String name, boolean on) {
        MAP.put(name, on);
    }

    public static boolean isDefaultOn(String name) {
        return !OFF_BY_DEFAULT.contains(name);
    }

    public static void removePrefix(String prefix) {
        Iterator<String> it = EXTRA.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(prefix)) it.remove();
        }
    }

    // ---------- aparencia ----------

    public static int accent() {
        return accent >= 0 ? ACCENTS[accent] : Theme.cur().accent;
    }

    public static int accentIndex() {
        return accent;
    }

    public static void setAccent(int i) {
        if (i >= -1 && i < ACCENTS.length) accent = i;
    }

    /** Cor de fundo dos paineis do HUD (opacidade ajustavel). */
    public static int bg() {
        float a = Math.max(0F, Math.min(1F, getFloat("opt.bg", 0.55F)));
        return ((int) (a * 255F) << 24) | Theme.cur().hudRgb;
    }

    /** Transparencia do menu do client (1 = opaco). */
    public static float guiAlpha() {
        return Math.max(0.25F, Math.min(1F, getFloat("opt.guiAlpha", 0.72F)));
    }

    public static float radius() {
        return Math.max(0F, Math.min(10F, getFloat("opt.radius", 4F)));
    }

    /** Escala final de um elemento do HUD (individual x geral). */
    public static float scale(String name) {
        float s = getFloat("scale." + name, 1F) * getFloat("scale.__all", 1F);
        return Math.max(0.3F, Math.min(3F, s));
    }
}
