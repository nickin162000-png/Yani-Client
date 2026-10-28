package com.yani.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/** Tela para arrastar os elementos do HUD para onde quiser. */
public class HudEditorGui extends GuiScreen {
    private final GuiScreen parent;
    private Hud.El drag;
    private Hud.El hover;
    private Hud.El resizing;
    private float offX, offY;
    private boolean guideV, guideH;
    private final float[] btnAnim = new float[2];

    public HudEditorGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void func_73866_w_() {
        Hud.editing = true;
    }

    @Override
    public void func_146281_b() {
        Hud.editing = false;
        Hud.savePositions();
    }

    private Hud.El hit(int mx, int my) {
        List<Hud.El> list = Hud.ELEMENTS;
        for (int i = list.size() - 1; i >= 0; i--) {
            Hud.El e = list.get(i);
            if (!Config.get(e.name) || e.w <= 0) continue;
            if (RenderUtil.hover(mx, my, e.x - 3, e.y - 3, e.x + e.w + 3, e.y + e.h + 3)) return e;
        }
        return null;
    }

    /** Quadradinho no canto inferior direito: arraste para mudar o tamanho. */
    private float[] handle(Hud.El e) {
        float x2 = e.x + e.w + 3;
        float y2 = e.y + e.h + 3;
        return new float[]{x2 - 5, y2 - 5, x2 + 5, y2 + 5};
    }

    private Hud.El handleAt(int mx, int my) {
        List<Hud.El> list = Hud.ELEMENTS;
        for (int i = list.size() - 1; i >= 0; i--) {
            Hud.El e = list.get(i);
            if (!Config.get(e.name) || e.w <= 0) continue;
            float[] h = handle(e);
            if (RenderUtil.hover(mx, my, h[0] - 2, h[1] - 2, h[2] + 2, h[3] + 2)) return e;
        }
        return null;
    }

    private float[] resetBounds() {
        return new float[]{field_146294_l / 2F - 115, field_146295_m - 40, field_146294_l / 2F - 5, field_146295_m - 18};
    }

    private float[] doneBounds() {
        return new float[]{field_146294_l / 2F + 5, field_146295_m - 40, field_146294_l / 2F + 115, field_146295_m - 18};
    }

    private void outline(float x, float y, float x2, float y2, int c) {
        RenderUtil.rect(x, y, x2, y + 1, c);
        RenderUtil.rect(x, y2 - 1, x2, y2, c);
        RenderUtil.rect(x, y, x + 1, y2, c);
        RenderUtil.rect(x2 - 1, y, x2, y2, c);
    }

    @Override
    public void func_73863_a(int mx, int my, float partialTicks) {
        Gui.func_73734_a(0, 0, field_146294_l, field_146295_m, 0x55000000);
        int accent = Config.accent();
        if (drag == null && resizing == null) hover = hit(mx, my);
        Hud.El overHandle = handleAt(mx, my);

        for (Hud.El e : Hud.ELEMENTS) {
            if (!Config.get(e.name) || e.w <= 0) continue;
            boolean sel = e == drag;
            boolean hov = e == hover;
            float x1 = e.x - 3, y1 = e.y - 3, x2 = e.x + e.w + 3, y2 = e.y + e.h + 3;
            RenderUtil.roundedRect(x1, y1, x2, y2, 5, sel ? RenderUtil.withAlpha(accent, 0x40) : (hov ? 0x30FFFFFF : 0x18FFFFFF));
            outline(x1, y1, x2, y2, sel ? accent : (hov ? 0xFFFFFFFF : 0x80FFFFFF));
            float ly = e.y < 14 ? y2 + 6 : y1 - 6;
            String lbl = e.name + "  " + Math.round(e.scale * 100F) + "%";
            Fonts.drawC(lbl, x1, ly, sel ? accent : 0xFFE0E0EA, 0.75F, true, true);

            // alca de redimensionar
            float[] hb = handle(e);
            boolean hh = e == resizing || e == overHandle;
            RenderUtil.roundedRect(hb[0], hb[1], hb[2], hb[3], 2, hh ? accent : 0xAAFFFFFF);
            RenderUtil.roundedRect(hb[0] + 2.5F, hb[1] + 2.5F, hb[2] - 2.5F, hb[3] - 2.5F, 1, 0xFF14141A);
        }

        if (drag != null) {
            if (guideV) RenderUtil.rect(field_146294_l / 2F - 0.5F, 0, field_146294_l / 2F + 0.5F, field_146295_m, RenderUtil.withAlpha(accent, 0x90));
            if (guideH) RenderUtil.rect(0, field_146295_m / 2F - 0.5F, field_146294_l, field_146295_m / 2F + 0.5F, RenderUtil.withAlpha(accent, 0x90));
        }

        String hint = "Arraste para mover   |   Arraste o quadradinho do canto para mudar o tamanho   |   ESC conclui";
        float hw = Fonts.width(hint, 1F, false) + 20;
        RenderUtil.roundedRect(field_146294_l / 2F - hw / 2F, 10, field_146294_l / 2F + hw / 2F, 28, 6, RenderUtil.withAlpha(Theme.cur().hudRgb, 0xCC));
        Fonts.drawC(hint, field_146294_l / 2F - hw / 2F + 10, 19, 0xFFFFFFFF, 1F, false, false);

        float[] rb = resetBounds();
        float[] db = doneBounds();
        btnAnim[0] = Ui.approach(btnAnim[0], RenderUtil.hover(mx, my, rb[0], rb[1], rb[2], rb[3]));
        btnAnim[1] = Ui.approach(btnAnim[1], RenderUtil.hover(mx, my, db[0], db[1], db[2], db[3]));
        Ui.button("Resetar tudo", rb[0], rb[1], rb[2] - rb[0], rb[3] - rb[1], btnAnim[0], false);
        Ui.button("Concluir", db[0], db[1], db[2] - db[0], db[3] - db[1], btnAnim[1], true);
    }

    @Override
    protected void func_73864_a(int mx, int my, int btn) throws IOException {
        super.func_73864_a(mx, my, btn);
        if (btn != 0) return;

        float[] rb = resetBounds();
        float[] db = doneBounds();
        if (RenderUtil.hover(mx, my, rb[0], rb[1], rb[2], rb[3])) {
            Hud.resetAll();
            Hud.savePositions();
            return;
        }
        if (RenderUtil.hover(mx, my, db[0], db[1], db[2], db[3])) {
            field_146297_k.func_147108_a(parent);
            return;
        }

        Hud.El rs = handleAt(mx, my);
        if (rs != null) {
            resizing = rs;
            return;
        }

        Hud.El e = hit(mx, my);
        if (e != null) {
            drag = e;
            offX = mx - e.x;
            offY = my - e.y;
        }
    }

    @Override
    protected void func_146273_a(int mx, int my, int btn, long time) {
        if (resizing != null) {
            float all = Config.getFloat("scale.__all", 1F);
            float base = Math.max(1F, resizing.bw) * all;
            float s = (mx - resizing.x) / base;
            s = Math.max(0.5F, Math.min(2F, Math.round(s * 20F) / 20F));
            Config.setFloat("scale." + resizing.name, s);
            return;
        }
        if (drag == null) return;
        float w = drag.w;
        float h = drag.h;
        float nx = mx - offX;
        float ny = my - offY;
        guideV = false;
        guideH = false;

        if (Math.abs(nx) < 5) nx = 0;
        if (Math.abs(nx + w - field_146294_l) < 5) nx = field_146294_l - w;
        if (Math.abs(nx + w / 2F - field_146294_l / 2F) < 5) {
            nx = field_146294_l / 2F - w / 2F;
            guideV = true;
        }
        if (Math.abs(ny) < 5) ny = 0;
        if (Math.abs(ny + h - field_146295_m) < 5) ny = field_146295_m - h;
        if (Math.abs(ny + h / 2F - field_146295_m / 2F) < 5) {
            ny = field_146295_m / 2F - h / 2F;
            guideH = true;
        }

        nx = Math.max(0, Math.min(field_146294_l - w, nx));
        ny = Math.max(0, Math.min(field_146295_m - h, ny));
        drag.fx = nx / field_146294_l;
        drag.fy = ny / field_146295_m;
    }

    @Override
    protected void func_146286_b(int mx, int my, int state) {
        super.func_146286_b(mx, my, state);
        if (resizing != null) {
            resizing = null;
            Config.save();
        }
        if (drag != null) {
            drag = null;
            guideV = false;
            guideH = false;
            Hud.savePositions();
        }
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            field_146297_k.func_147108_a(parent);
            return;
        }
        boolean plus = typedChar == '+' || typedChar == '=' || keyCode == Keyboard.KEY_ADD;
        boolean minus = typedChar == '-' || keyCode == Keyboard.KEY_SUBTRACT;
        Hud.El target = drag != null ? drag : hover;
        if ((plus || minus) && target != null) {
            float cur = Config.getFloat("scale." + target.name, 1F);
            cur += plus ? 0.05F : -0.05F;
            cur = Math.max(0.5F, Math.min(2F, Math.round(cur * 20F) / 20F));
            Config.setFloat("scale." + target.name, cur);
            Config.save();
            return;
        }
        super.func_73869_a(typedChar, keyCode);
    }

    @Override
    public boolean func_73868_f() {
        return false;
    }
}
