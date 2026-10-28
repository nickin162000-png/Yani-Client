package com.yani.client;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;

import java.io.IOException;

public class ModernPauseMenu extends GuiScreen {
    private final float[] hover = new float[5];

    private String label(int i) {
        switch (i) {
            case 0:
                return "Voltar ao jogo";
            case 1:
                return "Menu do Client";
            case 2:
                return "Editar HUD";
            case 3:
                return "Opcoes";
            default:
                return field_146297_k.func_71387_A() ? "Salvar e sair" : "Desconectar";
        }
    }

    private float top() {
        return field_146295_m / 2F - 118;
    }

    private float bx() {
        return field_146294_l / 2F - 100;
    }

    private float by(int i) {
        return top() + 78 + i * 30;
    }

    @Override
    public void func_73863_a(int mx, int my, float partialTicks) {
        Theme.T th = Theme.cur();
        RenderUtil.gradient(0, 0, field_146294_l, field_146295_m,
                RenderUtil.withAlpha(th.menuTop, 0xB4), RenderUtil.withAlpha(th.menuBottom, 0x8C));

        // painel translucido com a marca do client
        float panelX = field_146294_l / 2F - 118;
        float panelY = top();
        float panelH = 236;
        RenderUtil.roundedRect(panelX - 1, panelY - 1, panelX + 236 + 1, panelY + panelH + 1, 11, th.line);
        RenderUtil.roundedRect(panelX, panelY, panelX + 236, panelY + panelH, 10, Theme.ga(th.panel));

        int accent = Config.accent();
        Ui.logo(field_146294_l / 2F - 18, panelY + 10, 36, accent);
        String ya = "YANI";
        String yb = " CLIENT";
        float wa = Fonts.width(ya, 1.25F, true);
        float wb = Fonts.width(yb, 1.25F, false);
        float x0 = field_146294_l / 2F - (wa + wb) / 2F;
        Fonts.drawC(ya, x0, panelY + 58, 0xFFFFFFFF, 1.25F, true, false);
        Fonts.drawC(yb, x0 + wa, panelY + 58, 0xFFC8CCE0, 1.25F, false, false);

        for (int i = 0; i < 5; i++) {
            boolean hov = RenderUtil.hover(mx, my, bx(), by(i), bx() + 200, by(i) + 24);
            hover[i] = Ui.approach(hover[i], hov);
            Ui.button(label(i), bx(), by(i), 200, 24, hover[i], i == 0);
        }
    }

    @Override
    protected void func_73864_a(int mx, int my, int btn) throws IOException {
        super.func_73864_a(mx, my, btn);
        if (btn != 0) return;
        for (int i = 0; i < 5; i++) {
            if (!RenderUtil.hover(mx, my, bx(), by(i), bx() + 200, by(i) + 24)) continue;
            if (i == 0) {
                field_146297_k.func_147108_a(null);
                field_146297_k.func_71381_h();
            } else if (i == 1) {
                field_146297_k.func_147108_a(new ModernGui());
            } else if (i == 2) {
                field_146297_k.func_147108_a(new HudEditorGui(this));
            } else if (i == 3) {
                field_146297_k.func_147108_a(new GuiOptions(this, field_146297_k.field_71474_y));
            } else {
                boolean singleplayer = field_146297_k.func_71387_A();
                field_146297_k.field_71441_e.func_72882_A();
                field_146297_k.func_71403_a((WorldClient) null);
                if (singleplayer) field_146297_k.func_147108_a(new GuiMainMenu());
                else field_146297_k.func_147108_a(new GuiMultiplayer(new GuiMainMenu()));
            }
            return;
        }
    }

    @Override
    public boolean func_73868_f() {
        return true;
    }
}
