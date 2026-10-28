package com.yani.client;

import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Avisos curtos no topo da tela (ex.: "Toggle Sneak: LIGADO"). */
public class Toast {
    private static final long LIFE = 2200L;
    private static final List<String> TEXTS = new ArrayList<String>();
    private static final List<Long> TIMES = new ArrayList<Long>();

    public static void show(String text) {
        if (!Config.get("Notificacoes")) return;
        TEXTS.add(text);
        TIMES.add(System.currentTimeMillis());
        while (TEXTS.size() > 3) {
            TEXTS.remove(0);
            TIMES.remove(0);
        }
    }

    public static void render(ScaledResolution sr) {
        long now = System.currentTimeMillis();
        Iterator<Long> it = TIMES.iterator();
        int idx = 0;
        while (it.hasNext()) {
            if (now - it.next() > LIFE) {
                it.remove();
                TEXTS.remove(idx);
            } else {
                idx++;
            }
        }
        float cx = sr.func_78326_a() / 2F;
        for (int i = 0; i < TEXTS.size(); i++) {
            long age = now - TIMES.get(i);
            float in = Math.min(1F, age / 220F);
            float out = Math.min(1F, (LIFE - age) / 350F);
            float a = Math.max(0F, Math.min(in, out));
            if (a < 0.06F) continue;

            String t = TEXTS.get(i);
            float tw = Fonts.width(t, 1F, true);
            float w = tw + 26;
            float h = 18;
            float x = cx - w / 2F;
            float y = 10 + i * (h + 4) - (1F - in) * 8F;
            int alpha = (int) (255 * a);
            RenderUtil.roundedRect(x, y, x + w, y + h, 6, RenderUtil.withAlpha(Theme.cur().hudRgb, (int) (210 * a)));
            RenderUtil.roundedRect(x + 6, y + 5, x + 9, y + h - 5, 1.2F, RenderUtil.withAlpha(Config.accent(), alpha));
            Fonts.drawC(t, x + 15, y + h / 2F, RenderUtil.withAlpha(0xFFFFFF, alpha), 1F, true, false);
        }
    }
}
