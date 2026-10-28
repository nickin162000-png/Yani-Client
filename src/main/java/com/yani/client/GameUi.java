package com.yani.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Substitui a interface padrao do jogo (hotbar, vida, fome, xp, mira). */
public class GameUi {
    private static float selX = -1F;
    private static float animHp = -1F;
    private static float animFood = -1F;

    private static float clamp(float v) {
        return Math.max(0F, Math.min(1F, v));
    }

    // ---------- hotbar ----------

    public static void hotbar(Minecraft mc, ScaledResolution sr) {
        int sw = sr.func_78326_a();
        int sh = sr.func_78328_b();
        int slot = 20;
        int gap = 2;
        float total = 9 * slot + 8 * gap;
        float x0 = sw / 2F - total / 2F;
        float top = sh - 27;

        RenderUtil.roundedRect(x0 - 5, sh - 30, x0 + total + 5, sh - 4, 7, Config.bg());

        int cur = mc.field_71439_g.field_71071_by.field_70461_c;
        float target = x0 + cur * (slot + gap);
        if (selX < 0F) selX = target;
        selX += (target - selX) * 0.35F;

        for (int i = 0; i < 9; i++) {
            float sx = x0 + i * (slot + gap);
            RenderUtil.roundedRect(sx, top, sx + slot, top + slot, 4, 0x22FFFFFF);
        }
        RenderUtil.roundedRect(selX, top, selX + slot, top + slot, 4,
                RenderUtil.withAlpha(Config.accent(), 0xD0));

        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        RenderHelper.func_74520_c();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.field_71439_g.field_71071_by.field_70462_a[i];
            if (stack == null) continue;
            int ix = (int) (x0 + i * (slot + gap)) + 2;
            int iy = (int) top + 2;
            mc.func_175599_af().func_180450_b(stack, ix, iy);
            mc.func_175599_af().func_175030_a(mc.field_71466_p, stack, ix, iy);
        }
        RenderHelper.func_74518_a();
        GlStateManager.func_179140_f();
        GlStateManager.func_179147_l();
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    // ---------- barras de status ----------

    public static void status(Minecraft mc, ScaledResolution sr) {
        EntityPlayer p = mc.field_71439_g;
        int sw = sr.func_78326_a();
        int sh = sr.func_78328_b();
        float x0 = sw / 2F - 103;
        float full = 206;
        float barW = 96;
        float rightX = x0 + full - barW;

        // XP
        float xpY = sh - 35;
        RenderUtil.roundedRect(x0, xpY, x0 + full, xpY + 3, 1.5F, Config.bg());
        float xp = clamp(p.field_71106_cc);
        if (xp > 0.01F) RenderUtil.roundedRect(x0, xpY, x0 + Math.max(3F, full * xp), xpY + 3, 1.5F, 0xFF2ED573);

        // vida
        float hy = sh - 48;
        float hh = 10;
        float hp = p.func_110143_aJ();
        float max = Math.max(1F, p.func_110138_aP());
        float hpFrac = clamp(hp / max);
        if (animHp < 0F) animHp = hpFrac;
        animHp += (hpFrac - animHp) * 0.15F;
        String hpText = String.valueOf((int) Math.ceil(hp));
        float abs = p.func_110139_bj();
        if (abs > 0F) hpText += " +" + (int) Math.ceil(abs);
        bar(x0, hy, barW, hh, animHp, 0xFFFF4757, hpText);

        // fome
        float foodFrac = clamp(p.func_71024_bL().func_75116_a() / 20F);
        if (animFood < 0F) animFood = foodFrac;
        animFood += (foodFrac - animFood) * 0.15F;
        bar(rightX, hy, barW, hh, animFood, 0xFFFFA502, String.valueOf(p.func_71024_bL().func_75116_a()));

        // nivel de xp (entre as barras)
        if (p.field_71068_ca > 0) {
            String lv = String.valueOf(p.field_71068_ca);
            float lw = Fonts.width(lv, 0.85F, true);
            Fonts.drawC(lv, sw / 2F - lw / 2F, hy + hh / 2F, 0xFF9BFFB8, 0.85F, true, true);
        }

        // armadura (linha fina acima da vida)
        int armor = p.func_70658_aO();
        if (armor > 0) {
            RenderUtil.roundedRect(x0, hy - 5, x0 + barW, hy - 2, 1.5F, Config.bg());
            RenderUtil.roundedRect(x0, hy - 5, x0 + Math.max(3F, barW * clamp(armor / 20F)), hy - 2, 1.5F, 0xFF9FB4CC);
        }

        // ar (debaixo d'agua)
        if (p.func_70055_a(Material.field_151586_h)) {
            float air = clamp(p.func_70086_ai() / 300F);
            RenderUtil.roundedRect(rightX, hy - 6, rightX + barW, hy - 2, 2F, Config.bg());
            RenderUtil.roundedRect(rightX, hy - 6, rightX + Math.max(4F, barW * air), hy - 2, 2F, 0xFF3EA6FF);
        }

        GlStateManager.func_179147_l();
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    private static void bar(float x, float y, float w, float h, float frac, int color, String text) {
        RenderUtil.roundedRect(x, y, x + w, y + h, h / 2F, Config.bg());
        if (frac > 0.01F) RenderUtil.roundedRect(x, y, x + Math.max(h, w * clamp(frac)), y + h, h / 2F, color);
        float tw = Fonts.width(text, 0.8F, true);
        Fonts.drawC(text, x + (w - tw) / 2F, y + h / 2F, 0xFFFFFFFF, 0.8F, true, true);
    }

    // ---------- mira ----------

    public static void crosshair(ScaledResolution sr) {
        Crosshair.render(sr.func_78326_a() / 2F, sr.func_78328_b() / 2F);
    }
}
