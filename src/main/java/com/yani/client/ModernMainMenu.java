package com.yani.client;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ModernMainMenu extends GuiScreen {
    private static final String[] LABELS = {"Um jogador", "Multijogador", "Opcoes", "Sair do jogo"};

    // botao "Temas" (canto superior direito) e a lista que abre embaixo dele
    private static final float TBW = 108F;
    private static final float TBH = 22F;
    private static final float DDW = 176F;
    private static final float CARDH = 80F;

    private final float[] hover = new float[LABELS.length];
    private final float[] cardHover = new float[Theme.ALL.length];
    private float themeBtnHover = 0F;
    private float openAnim = 0F;
    private boolean open = false;
    private long changeTime = 0L;

    private float bw() {
        return 200;
    }

    private float bx() {
        return field_146294_l / 2F - bw() / 2F;
    }

    private float by(int i) {
        return field_146295_m / 2F - 20 + i * 34;
    }

    // ---------- posicoes do botao Temas e da lista ----------

    private float tbX() {
        return field_146294_l - 12 - TBW;
    }

    private float tbY() {
        return 10;
    }

    private float ddX() {
        return field_146294_l - 12 - DDW;
    }

    private float ddY() {
        return tbY() + TBH + 6;
    }

    private float ddH() {
        return 8 + Theme.ALL.length * (CARDH + 6);
    }

    private float cardY(int i) {
        return ddY() + 8 + i * (CARDH + 6);
    }

    @Override
    public void func_73863_a(int mx, int my, float partialTicks) {
        int accent = Config.accent();
        Theme.T th = Theme.cur();
        MenuBg.full(MenuBg.style(), field_146294_l, field_146295_m, accent);

        // titulo
        String a = "Yani ";
        String b = "Client";
        float aw = Fonts.width(a, 3F, true);
        float bwid = Fonts.width(b, 3F, true);
        float tx = field_146294_l / 2F - (aw + bwid) / 2F;
        float ty = by(0) - 62;
        Ui.logo(field_146294_l / 2F - 18, Math.max(4F, ty - 44), 36, accent);
        Fonts.drawC(a, tx, ty, 0xFFFFFFFF, 3F, true, true);
        Fonts.drawC(b, tx + aw, ty, accent, 3F, true, true);

        String sub = "CLIENT 1.0   |   Minecraft 1.8.9";
        float sw = Fonts.width(sub, 0.9F, false);
        Fonts.drawC(sub, field_146294_l / 2F - sw / 2F, ty + 26, th.textDim, 0.9F, false, false);

        // botoes (translucidos, estilo Lunar)
        for (int i = 0; i < LABELS.length; i++) {
            boolean hov = !(open && overDropdown(mx, my)) && RenderUtil.hover(mx, my, bx(), by(i), bx() + bw(), by(i) + 26);
            hover[i] = Ui.approach(hover[i], hov);
            Ui.button(LABELS[i], bx(), by(i), bw(), 26, hover[i], i == 0);
        }

        // botao Temas
        boolean hovBtn = RenderUtil.hover(mx, my, tbX(), tbY(), tbX() + TBW, tbY() + TBH);
        themeBtnHover = Ui.approach(themeBtnHover, hovBtn || open);
        Ui.button("Temas", tbX(), tbY(), TBW, TBH, themeBtnHover, open);
        for (int i = 0; i < Theme.ALL.length; i++) {
            RenderUtil.circle(tbX() + 14 + i * 8, tbY() + TBH / 2F, 2.6F, Theme.ALL[i].accent);
        }
        float cx = tbX() + TBW - 10;
        float cy = tbY() + TBH / 2F;
        int caret = (open && Ui.isLight(accent)) ? 0xFF111114 : 0xFFFFFFFF;
        if (open) RenderUtil.triangle(cx - 4, cy + 2, cx + 4, cy + 2, cx, cy - 2.5F, caret);
        else RenderUtil.triangle(cx - 4, cy - 2, cx + 4, cy - 2, cx, cy + 2.5F, caret);

        drawDropdown(mx, my, accent, th);

        // flash ao trocar de tema
        long age = System.currentTimeMillis() - changeTime;
        if (changeTime > 0 && age < 500) {
            float f = 1F - age / 500F;
            RenderUtil.rect(0, 0, field_146294_l, field_146295_m, RenderUtil.withAlpha(accent, (int) (120 * f * f)));
        }
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    private boolean overDropdown(int mx, int my) {
        return RenderUtil.hover(mx, my, ddX(), ddY(), ddX() + DDW, ddY() + ddH());
    }

    private void drawDropdown(int mx, int my, int accent, Theme.T th) {
        openAnim += ((open ? 1F : 0F) - openAnim) * 0.3F;
        if (!open && openAnim < 0.03F) {
            openAnim = 0F;
            return;
        }
        float x = ddX();
        float y = ddY();
        float h = ddH();

        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b(x + DDW, y, 0F);
        GlStateManager.func_179152_a(1F, Math.max(0.05F, openAnim), 1F);
        GlStateManager.func_179109_b(-(x + DDW), -y, 0F);

        RenderUtil.roundedRect(x - 1, y - 1, x + DDW + 1, y + h + 1, 11, th.line);
        RenderUtil.roundedRect(x, y, x + DDW, y + h, 10, Theme.ga(th.panel));

        for (int i = 0; i < Theme.ALL.length; i++) {
            Theme.T t = Theme.ALL[i];
            float cxp = x + 8;
            float cyp = cardY(i);
            float cwp = DDW - 16;
            boolean sel = i == Theme.index();
            boolean hov = open && RenderUtil.hover(mx, my, cxp, cyp, cxp + cwp, cyp + CARDH);
            cardHover[i] = Ui.approach(cardHover[i], hov);

            if (sel) RenderUtil.roundedRect(cxp - 1.5F, cyp - 1.5F, cxp + cwp + 1.5F, cyp + CARDH + 1.5F, 8, t.accent);
            else if (hov) RenderUtil.roundedRect(cxp - 1, cyp - 1, cxp + cwp + 1, cyp + CARDH + 1, 7.5F, 0x66FFFFFF);
            RenderUtil.roundedRect(cxp, cyp, cxp + cwp, cyp + CARDH, 7, RenderUtil.lerpColor(th.card, th.cardHover, cardHover[i]));

            MenuBg.thumb(i, cxp + 4, cyp + 4, cwp - 8, 46, t.accent);
            RenderUtil.circle(cxp + 12, cyp + 61, 3.2F, t.accent);
            Fonts.drawC(t.name, cxp + 20, cyp + 61, sel ? 0xFFFFFFFF : 0xFFE0E0EA, 0.95F, true, false);
            Fonts.drawC(t.desc, cxp + 8, cyp + 72, th.textDim, 0.68F, false, false);
        }
        GlStateManager.func_179121_F();
    }

    @Override
    protected void func_73864_a(int mx, int my, int btn) throws IOException {
        super.func_73864_a(mx, my, btn);
        if (btn != 0) return;

        // botao Temas
        if (RenderUtil.hover(mx, my, tbX(), tbY(), tbX() + TBW, tbY() + TBH)) {
            open = !open;
            return;
        }

        if (open) {
            for (int i = 0; i < Theme.ALL.length; i++) {
                float cxp = ddX() + 8;
                float cyp = cardY(i);
                if (RenderUtil.hover(mx, my, cxp, cyp, cxp + DDW - 16, cyp + CARDH)) {
                    if (i != Theme.index()) {
                        Theme.set(i);
                        changeTime = System.currentTimeMillis();
                    }
                    return;
                }
            }
            if (overDropdown(mx, my)) return;
            open = false; // clicou fora: fecha a lista (e nao aperta o botao de baixo)
            return;
        }

        for (int i = 0; i < LABELS.length; i++) {
            if (!RenderUtil.hover(mx, my, bx(), by(i), bx() + bw(), by(i) + 26)) continue;
            if (i == 0) field_146297_k.func_147108_a(new GuiSelectWorld(this));
            else if (i == 1) field_146297_k.func_147108_a(new GuiMultiplayer(this));
            else if (i == 2) field_146297_k.func_147108_a(new GuiOptions(this, field_146297_k.field_71474_y));
            else field_146297_k.func_71400_g();
            return;
        }
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        // sem ESC na tela inicial (so fecha a lista de temas)
        if (keyCode == Keyboard.KEY_ESCAPE && open) open = false;
    }

    @Override
    public boolean func_73868_f() {
        return false;
    }
}
