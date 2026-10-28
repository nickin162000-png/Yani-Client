package com.yani.client;

/**
 * Temas completos do client. Cada tema muda a tela inicial, o menu do client,
 * o menu de pausa, os botoes, o HUD e a cor de destaque.
 */
public class Theme {

    public static class T {
        public final String name, desc;
        public final int accent, menuTop, menuBottom, panel, side, card, cardHover, track, off, btn, btnHover, line, hudRgb, textDim;

        T(String name, String desc, int accent, int menuTop, int menuBottom, int panel, int side, int card,
          int cardHover, int track, int off, int btn, int btnHover, int line, int hudRgb, int textDim) {
            this.name = name;
            this.desc = desc;
            this.accent = accent;
            this.menuTop = menuTop;
            this.menuBottom = menuBottom;
            this.panel = panel;
            this.side = side;
            this.card = card;
            this.cardHover = cardHover;
            this.track = track;
            this.off = off;
            this.btn = btn;
            this.btnHover = btnHover;
            this.line = line;
            this.hudRgb = hudRgb;
            this.textDim = textDim;
        }
    }

    public static final T[] ALL = {
            new T("Meia-Noite", "Ceu estrelado e cidade acesa",
                    0xFF7C5CFF, 0xFF05050B, 0xFF16112B,
                    0xD80C0C12, 0xC0121218, 0xB0181820, 0xC8262634, 0xFF2A2A36, 0xFF3A3A48,
                    0xB41E1E26, 0xD22D2D39, 0x26FFFFFF, 0x0E0E12, 0xFF8A8A98),
            new T("Abobora Maldita", "Lua laranja, morcegos e abobora",
                    0xFFFF7A00, 0xFF0D0408, 0xFF3A1406,
                    0xD81A0A06, 0xC0240F08, 0xB02E140B, 0xC84A2010, 0xFF3C2016, 0xFF553322,
                    0xB42C130B, 0xD25A2810, 0x40FF7A00, 0x1A0A06, 0xFFA8826C),
            new T("Gelo Eterno", "Aurora boreal e neve nas montanhas",
                    0xFF35B8F0, 0xFF030D18, 0xFF0F3552,
                    0xD80A1824, 0xC0102334, 0xB0143048, 0xC81F4668, 0xFF24425C, 0xFF35566F,
                    0xB4143048, 0xD2235A80, 0x4035B8F0, 0x08161F, 0xFF86A9C2)
    };

    public static int index() {
        int i = Config.getInt("theme", 0);
        return i < 0 || i >= ALL.length ? 0 : i;
    }

    public static T cur() {
        return ALL[index()];
    }

    public static T get(int i) {
        return ALL[i < 0 || i >= ALL.length ? 0 : i];
    }

    /** Troca o tema e volta para a cor de destaque do tema. */
    public static void set(int i) {
        if (i < 0 || i >= ALL.length) return;
        Config.setInt("theme", i);
        Config.setAccent(-1);
        Config.save();
    }

    /** Aplica a transparencia escolhida do menu a uma cor. */
    public static int ga(int argb) {
        int a = (int) (((argb >>> 24) & 255) * Config.guiAlpha());
        return (a << 24) | (argb & 0xFFFFFF);
    }
}
