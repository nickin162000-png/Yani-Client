package com.yani.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Hud {

    public static boolean editing = false;
    public static final List<El> ELEMENTS = new ArrayList<El>();

    /** Velocidade horizontal em blocos por segundo (suavizada). */
    public static float speed = 0F;
    public static boolean airborne = false;

    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yyyy");
    private static final List<Long> LEFT = new ArrayList<Long>();
    private static final List<Long> RIGHT = new ArrayList<Long>();
    private static final float[] KEY_ANIM = new float[7];

    private static double lastX, lastZ;
    private static boolean lastValid = false;
    private static int combo = 0;
    private static long lastHitTime = 0L;
    private static float reach = 0F;
    private static long reachTime = 0L;
    private static int lastHurt = 0;

    // estatisticas novas
    public static boolean hidden = false;
    private static final long START = System.currentTimeMillis();
    private static final float[] FPS_HIST = new float[48];
    private static long fpsSample = 0L;
    private static EntityLivingBase target = null;
    private static long targetTime = 0L;
    private static float targetAnim = -1F;
    private static EntityLivingBase attacked = null;
    private static boolean killCounted = true;
    private static int hitsDealt = 0, hitsTaken = 0, kills = 0, deaths = 0;
    private static float lastHp = -1F;
    private static Object lastWorld = null;

    /** Um elemento do HUD que pode ser arrastado. fx/fy = posicao como fracao da tela. */
    public static class El {
        public final String name;
        private final float dfx, dfy;
        public float fx, fy;
        /** x, y, w, h: posicao e tamanho NA TELA (ja com a escala). bw/bh: tamanho sem escala. */
        public float x, y, w, h;
        public float bw, bh;
        public float scale = 1F;

        El(String name, float px, float py) {
            this.name = name;
            this.dfx = px / 960F;
            this.dfy = py / 540F;
            this.fx = Config.getFloat("pos." + name + ".x", dfx);
            this.fy = Config.getFloat("pos." + name + ".y", dfy);
        }

        public void reset() {
            fx = dfx;
            fy = dfy;
        }
    }

    public static void init() {
        ELEMENTS.clear();
        ELEMENTS.add(new El("Marca", 4, 4));
        ELEMENTS.add(new El("FPS", 4, 24));
        ELEMENTS.add(new El("CPS", 4, 42));
        ELEMENTS.add(new El("Ping", 4, 60));
        ELEMENTS.add(new El("Coordenadas", 4, 78));
        ELEMENTS.add(new El("Direcao", 4, 96));
        ELEMENTS.add(new El("Relogio", 4, 114));
        ELEMENTS.add(new El("Velocidade", 4, 132));
        ELEMENTS.add(new El("Memoria", 4, 150));
        ELEMENTS.add(new El("Data", 4, 168));
        ELEMENTS.add(new El("Sessao", 4, 186));
        ELEMENTS.add(new El("Rotacao", 4, 204));
        ELEMENTS.add(new El("Grafico FPS", 4, 222));
        ELEMENTS.add(new El("Keystrokes", 4, 440));
        ELEMENTS.add(new El("Teclas Mouse", 4, 508));
        ELEMENTS.add(new El("Armor HUD", 902, 225));
        ELEMENTS.add(new El("Efeitos", 840, 4));
        ELEMENTS.add(new El("Combo", 440, 292));
        ELEMENTS.add(new El("Reach", 440, 310));
        ELEMENTS.add(new El("TNT", 440, 328));
        ELEMENTS.add(new El("Itens", 880, 345));
        ELEMENTS.add(new El("Alvo", 417, 380));
        ELEMENTS.add(new El("Hits", 440, 346));
        ELEMENTS.add(new El("Abates", 440, 364));
    }

    public static void resetAll() {
        for (El e : ELEMENTS) e.reset();
    }

    public static void savePositions() {
        for (El e : ELEMENTS) {
            Config.setFloat("pos." + e.name + ".x", e.fx);
            Config.setFloat("pos." + e.name + ".y", e.fy);
        }
        Config.save();
    }

    // ---------- eventos ----------

    public static void click(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) LEFT.add(now);
        else if (button == 1) RIGHT.add(now);
    }

    /** Chamado quando o jogador bate em uma entidade. */
    public static void hit(Minecraft mc, Entity victim) {
        long now = System.currentTimeMillis();
        combo++;
        hitsDealt++;
        lastHitTime = now;
        if (victim instanceof EntityLivingBase) {
            attacked = (EntityLivingBase) victim;
            killCounted = false;
            if (target != attacked) targetAnim = -1F;
            target = attacked;
            targetTime = now;
        }
        if (mc.field_71439_g == null) return;
        MovingObjectPosition mop = mc.field_71476_x;
        if (mop != null && mop.field_72307_f != null) {
            Vec3 v = mop.field_72307_f;
            double dx = v.field_72450_a - mc.field_71439_g.field_70165_t;
            double dy = v.field_72448_b - (mc.field_71439_g.field_70163_u + mc.field_71439_g.func_70047_e());
            double dz = v.field_72449_c - mc.field_71439_g.field_70161_v;
            reach = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            reachTime = now;
        }
    }

    /** Chamado todo tick do jogo. */
    public static void tick(Minecraft mc) {
        if (mc.field_71439_g == null) {
            lastValid = false;
            return;
        }
        if (mc.field_71441_e != lastWorld) {
            lastWorld = mc.field_71441_e;
            hitsDealt = 0;
            hitsTaken = 0;
            kills = 0;
            deaths = 0;
            lastHp = -1F;
            target = null;
            attacked = null;
        }
        double x = mc.field_71439_g.field_70165_t;
        double z = mc.field_71439_g.field_70161_v;
        if (lastValid) {
            double dx = x - lastX;
            double dz = z - lastZ;
            float v = (float) Math.sqrt(dx * dx + dz * dz) * 20F;
            speed += (v - speed) * 0.3F;
        }
        lastX = x;
        lastZ = z;
        lastValid = true;
        airborne = !mc.field_71439_g.field_70122_E;

        if (combo > 0 && System.currentTimeMillis() - lastHitTime > 2500L) combo = 0;
        int hurt = mc.field_71439_g.field_70737_aN;
        if (hurt > 0 && lastHurt == 0) {
            combo = 0;
            hitsTaken++;
        }
        lastHurt = hurt;

        // abates e mortes
        float hp = mc.field_71439_g.func_110143_aJ();
        if (lastHp > 0F && hp <= 0F) deaths++;
        lastHp = hp;
        if (attacked != null && !killCounted && attacked.func_110143_aJ() <= 0F) {
            killCounted = true;
            if (System.currentTimeMillis() - lastHitTime < 6000L) kills++;
        }

        // alvo: quem o jogador esta mirando
        MovingObjectPosition mop = mc.field_71476_x;
        if (mop != null && mop.field_72308_g instanceof EntityLivingBase && mop.field_72308_g != mc.field_71439_g) {
            if (mop.field_72308_g != target) targetAnim = -1F;
            target = (EntityLivingBase) mop.field_72308_g;
            targetTime = System.currentTimeMillis();
        }
    }

    private static void prune(List<Long> list, long now) {
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000) it.remove();
        }
    }

    // ---------- render ----------

    public static void render(Minecraft mc, ScaledResolution sr) {
        long now = System.currentTimeMillis();
        prune(LEFT, now);
        prune(RIGHT, now);

        float sw = sr.func_78326_a();
        float sh = sr.func_78328_b();

        if (Config.get("Flash de Dano")) damageFlash(mc, sw, sh);
        if (Config.get("Aviso Vida Baixa")) lowHealth(mc, sw, sh, now);

        if (hidden && !editing) {
            for (El e : ELEMENTS) e.w = 0;
            Toast.render(sr);
            return;
        }

        for (El e : ELEMENTS) {
            if (!Config.get(e.name)) {
                e.w = 0;
                continue;
            }
            float s = Config.scale(e.name);
            e.scale = s;
            e.x = e.fx * sw;
            e.y = e.fy * sh;
            e.bw = 0;
            e.bh = 0;

            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b(e.x, e.y, 0F);
            GlStateManager.func_179152_a(s, s, 1F);
            draw(e, mc, now);
            GlStateManager.func_179121_F();

            e.w = e.bw * s;
            e.h = e.bh * s;
        }
        Toast.render(sr);
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    private static void draw(El e, Minecraft mc, long now) {
        String n = e.name;
        if (n.equals("Marca")) brand(e);
        else if (n.equals("FPS")) pill(e, "FPS", String.valueOf(Minecraft.func_175610_ah()));
        else if (n.equals("CPS")) pill(e, "CPS", LEFT.size() + " | " + RIGHT.size());
        else if (n.equals("Ping")) pill(e, "Ping", ping(mc) + " ms");
        else if (n.equals("Coordenadas")) {
            pill(e, "XYZ", MathHelper.func_76128_c(mc.field_71439_g.field_70165_t) + " "
                    + MathHelper.func_76128_c(mc.field_71439_g.field_70163_u) + " "
                    + MathHelper.func_76128_c(mc.field_71439_g.field_70161_v));
        } else if (n.equals("Direcao")) pill(e, "Dir", mc.field_71439_g.func_174811_aO().func_176610_l());
        else if (n.equals("Relogio")) pill(e, "Hora", CLOCK.format(new Date()));
        else if (n.equals("Data")) pill(e, "Data", DATE.format(new Date()));
        else if (n.equals("Velocidade")) pill(e, "Vel", String.format("%.1f b/s", speed));
        else if (n.equals("Memoria")) pill(e, "RAM", memory());
        else if (n.equals("Keystrokes")) keystrokes(e, mc);
        else if (n.equals("Teclas Mouse")) mouseKeys(e);
        else if (n.equals("Armor HUD")) armor(e, mc);
        else if (n.equals("Efeitos")) effects(e, mc);
        else if (n.equals("Combo")) combo(e, now);
        else if (n.equals("Reach")) reach(e, now);
        else if (n.equals("TNT")) tnt(e, mc);
        else if (n.equals("Itens")) items(e, mc);
        else if (n.equals("Sessao")) pill(e, "Sessao", session(now));
        else if (n.equals("Rotacao")) pill(e, "Rot", rotation(mc));
        else if (n.equals("Grafico FPS")) fpsGraph(e, now);
        else if (n.equals("Alvo")) targetHud(e, mc, now);
        else if (n.equals("Hits")) pill(e, "Hits", hitsDealt + " | " + hitsTaken);
        else if (n.equals("Abates")) pill(e, "K/D", kills + " | " + deaths);
    }

    // ---------- novos elementos ----------

    private static String session(long now) {
        long sec = (now - START) / 1000L;
        long h = sec / 3600L;
        long m = (sec / 60L) % 60L;
        long s = sec % 60L;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }

    private static String rotation(Minecraft mc) {
        float yaw = ((mc.field_71439_g.field_70177_z % 360F) + 540F) % 360F - 180F;
        float pitch = mc.field_71439_g.field_70125_A;
        return String.format("%.0f / %.0f", yaw, pitch);
    }

    /** Mini grafico com os ultimos FPS (uma amostra a cada 250 ms). */
    private static void fpsGraph(El e, long now) {
        int fps = Minecraft.func_175610_ah();
        if (fpsSample == 0L) {
            for (int i = 0; i < FPS_HIST.length; i++) FPS_HIST[i] = fps;
            fpsSample = now;
        }
        if (now - fpsSample >= 250L) {
            System.arraycopy(FPS_HIST, 1, FPS_HIST, 0, FPS_HIST.length - 1);
            FPS_HIST[FPS_HIST.length - 1] = fps;
            fpsSample = now;
        }
        float max = 60F;
        for (float v : FPS_HIST) if (v > max) max = v;

        float w = FPS_HIST.length * 2F + 12F;
        float h = 46F;
        e.bw = w;
        e.bh = h;
        panel(w, h);
        boolean sh = Config.get("Sombra");
        String cur = String.valueOf(fps);
        float lw = Fonts.width("FPS ", 1F, false);
        Fonts.drawC("FPS ", 6, 9, 0xFFB4B4BE, 1F, false, sh);
        Fonts.drawC(cur, 6 + lw, 9, 0xFFFFFFFF, 1F, true, sh);
        float bottom = h - 5F;
        float top = 19F;
        for (int i = 0; i < FPS_HIST.length; i++) {
            float v = FPS_HIST[i];
            float frac = Math.max(0.04F, Math.min(1F, v / max));
            int col = RenderUtil.lerpColor(0xFFFF4757, 0xFF2ED573, Math.min(1F, v / 60F));
            RenderUtil.rect(6 + i * 2F, bottom - (bottom - top) * frac, 6 + i * 2F + 1.5F, bottom, col);
        }
    }

    /** Vida do inimigo que voce acertou ou esta mirando. */
    private static void targetHud(El e, Minecraft mc, long now) {
        EntityLivingBase t = target;
        boolean active = t != null && t.func_110143_aJ() > 0F && now - targetTime <= 4000L;
        String name;
        float hp, max, dist;
        if (active) {
            name = t.func_70005_c_();
            hp = t.func_110143_aJ();
            max = Math.max(1F, t.func_110138_aP());
            double dx = t.field_70165_t - mc.field_71439_g.field_70165_t;
            double dy = t.field_70163_u - mc.field_71439_g.field_70163_u;
            double dz = t.field_70161_v - mc.field_71439_g.field_70161_v;
            dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        } else if (editing) {
            name = "Jogador";
            hp = 14F;
            max = 20F;
            dist = 3.2F;
        } else {
            return;
        }
        float frac = Math.max(0F, Math.min(1F, hp / max));
        if (targetAnim < 0F) targetAnim = frac;
        targetAnim += (frac - targetAnim) * 0.2F;

        String d = String.format("%.1fm", dist);
        float nw = Fonts.width(name, 1F, true);
        float dw = Fonts.width(d, 0.8F, false);
        float w = Math.max(130F, nw + dw + 30F);
        float h = 36F;
        e.bw = w;
        e.bh = h;
        panel(w, h);
        boolean sh = Config.get("Sombra");
        Fonts.drawC(name, 8, 10, 0xFFFFFFFF, 1F, true, sh);
        Fonts.drawC(d, w - 8 - dw, 10, 0xFFB4B4BE, 0.8F, false, sh);

        float bx = 8F, by = 19F, bw = w - 16F, bh = 11F;
        RenderUtil.roundedRect(bx, by, bx + bw, by + bh, bh / 2F, 0xFF2A2A33);
        int col = RenderUtil.lerpColor(0xFFFF4757, 0xFF2ED573, frac);
        if (targetAnim > 0.01F) {
            RenderUtil.roundedRect(bx, by, bx + Math.max(bh, bw * targetAnim), by + bh, bh / 2F, col);
        }
        String txt = String.format("%.1f", hp);
        float tw = Fonts.width(txt, 0.8F, true);
        Fonts.drawC(txt, bx + (bw - tw) / 2F, by + bh / 2F, 0xFFFFFFFF, 0.8F, true, true);
    }

    private static int ping(Minecraft mc) {
        if (mc.func_147114_u() == null) return 0;
        NetworkPlayerInfo info = mc.func_147114_u().func_175102_a(mc.field_71439_g.func_110124_au());
        return info == null ? 0 : info.func_178853_c();
    }

    private static String memory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        return used + "/" + max + " MB";
    }

    // ---------- pecas basicas ----------

    private static void panel(float w, float h) {
        if (Config.get("Fundo HUD")) RenderUtil.roundedRect(0, 0, w, h, Config.radius(), Config.bg());
    }

    private static void brand(El e) {
        String t = "Yani Client";
        float w = Fonts.width(t, 1.1F, true) + 18;
        float h = 17;
        e.bw = w;
        e.bh = h;
        panel(w, h);
        RenderUtil.roundedRect(5, 4, 7, h - 4, 1F, Config.accent());
        Fonts.drawC(t, 11, h / 2F, Config.accent(), 1.1F, true, Config.get("Sombra"));
    }

    private static void pill(El e, String label, String value) {
        boolean sh = Config.get("Sombra");
        float lw = Fonts.width(label + " ", 1F, false);
        float vw = Fonts.width(value, 1F, true);
        float h = 15;
        float w = lw + vw + 12;
        e.bw = w;
        e.bh = h;
        panel(w, h);
        float cy = h / 2F;
        Fonts.drawC(label + " ", 6, cy, 0xFFB4B4BE, 1F, false, sh);
        Fonts.drawC(value, 6 + lw, cy, 0xFFFFFFFF, 1F, true, sh);
    }

    /** Lista de linhas "nome valor" empilhadas. */
    private static void rows(El e, List<String> left, List<String> right, int leftColor, int rightColor) {
        boolean sh = Config.get("Sombra");
        float y = 0;
        float maxW = 0;
        for (int i = 0; i < left.size(); i++) {
            String a = left.get(i) + " ";
            String b = right.get(i);
            float aw = Fonts.width(a, 1F, false);
            float bw = Fonts.width(b, 1F, true);
            float w = aw + bw + 12;
            float h = 15;
            panel0(0, y, w, y + h);
            Fonts.drawC(a, 6, y + h / 2F, leftColor, 1F, false, sh);
            Fonts.drawC(b, 6 + aw, y + h / 2F, rightColor, 1F, true, sh);
            if (w > maxW) maxW = w;
            y += h + 3;
        }
        e.bw = maxW;
        e.bh = Math.max(0F, y - 3);
    }

    private static void panel0(float x, float y, float x2, float y2) {
        if (Config.get("Fundo HUD")) RenderUtil.roundedRect(x, y, x2, y2, Config.radius(), Config.bg());
    }

    // ---------- keystrokes ----------

    /** Keystrokes: so as teclas W A S D e a barra de espaco (sem o mouse). */
    private static void keystrokes(El e, Minecraft mc) {
        int s = 22;
        int g = 3;
        e.bw = s * 3 + g * 2;
        e.bh = s * 2 + g * 2 + 12;

        boolean[] down = {
                mc.field_71474_y.field_74351_w.func_151470_d(),
                mc.field_71474_y.field_74370_x.func_151470_d(),
                mc.field_71474_y.field_74368_y.func_151470_d(),
                mc.field_71474_y.field_74366_z.func_151470_d(),
                mc.field_71474_y.field_74314_A.func_151470_d()
        };
        for (int i = 0; i < 4; i++) {
            KEY_ANIM[i] += ((down[i] ? 1F : 0F) - KEY_ANIM[i]) * 0.3F;
        }
        KEY_ANIM[6] += ((down[4] ? 1F : 0F) - KEY_ANIM[6]) * 0.3F;

        keyBox(0, "W", s + g, 0, s, s, null);
        keyBox(1, "A", 0, s + g, s, s, null);
        keyBox(2, "S", s + g, s + g, s, s, null);
        keyBox(3, "D", (s + g) * 2, s + g, s, s, null);

        float sy = (s + g) * 2;
        float sw = s * 3 + g * 2;
        int pressed = RenderUtil.withAlpha(Config.accent(), 0xD0);
        RenderUtil.roundedRect(0, sy, sw, sy + 12, Config.radius(),
                RenderUtil.lerpColor(boxBg(), pressed, KEY_ANIM[6]));
        int bar = RenderUtil.lerpColor(0xFFFFFFFF, Ui.isLight(Config.accent()) ? 0xFF111114 : 0xFFFFFFFF, KEY_ANIM[6]);
        RenderUtil.roundedRect(sw / 2F - 12, sy + 5, sw / 2F + 12, sy + 7, 1F, bar);
    }

    /** Teclas do mouse (LMB / RMB) como elemento separado. */
    private static void mouseKeys(El e) {
        int s = 22;
        int g = 3;
        e.bw = s * 3 + g * 2;
        e.bh = s;

        boolean[] down = {Mouse.isButtonDown(0), Mouse.isButtonDown(1)};
        for (int i = 0; i < 2; i++) {
            KEY_ANIM[4 + i] += ((down[i] ? 1F : 0F) - KEY_ANIM[4 + i]) * 0.3F;
        }
        float mw = (e.bw - g) / 2F;
        keyBox(4, "LMB", 0, 0, mw, s, LEFT.size() + " CPS");
        keyBox(5, "RMB", mw + g, 0, mw, s, RIGHT.size() + " CPS");
    }

    /** Teclas sempre tem um fundo discreto, mesmo com "Fundo HUD" desligado. */
    private static int boxBg() {
        return Config.get("Fundo HUD") ? Config.bg() : RenderUtil.withAlpha(Theme.cur().hudRgb, 0x66);
    }

    private static void keyBox(int idx, String label, float x, float y, float w, float h, String sub) {
        int pressed = RenderUtil.withAlpha(Config.accent(), 0xD0);
        RenderUtil.roundedRect(x, y, x + w, y + h, Config.radius(), RenderUtil.lerpColor(boxBg(), pressed, KEY_ANIM[idx]));
        boolean sh = Config.get("Sombra");
        int txt = RenderUtil.lerpColor(0xFFFFFFFF, Ui.isLight(Config.accent()) ? 0xFF111114 : 0xFFFFFFFF, KEY_ANIM[idx]);
        if (sub == null) {
            float tw = Fonts.width(label, 1F, true);
            Fonts.drawC(label, x + (w - tw) / 2F, y + h / 2F, txt, 1F, true, sh);
        } else {
            float tw = Fonts.width(label, 0.9F, true);
            Fonts.drawC(label, x + (w - tw) / 2F, y + 8, txt, 0.9F, true, sh);
            float sw = Fonts.width(sub, 0.7F, false);
            Fonts.drawC(sub, x + (w - sw) / 2F, y + h - 6, 0xFFC8C8D2, 0.7F, false, sh);
        }
    }

    // ---------- armor ----------

    private static void armor(El e, Minecraft mc) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(mc.field_71439_g.func_70694_bm());
        for (int i = 3; i >= 0; i--) stacks.add(mc.field_71439_g.field_71071_by.field_70460_b[i]);

        e.bw = 52;
        e.bh = stacks.size() * 18 + 6;
        panel0(26, 0, 52, e.bh);

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            float iy = 4 + i * 18;
            if (stack == null) continue;

            GlStateManager.func_179094_E();
            RenderHelper.func_74520_c();
            mc.func_175599_af().func_180450_b(stack, 31, (int) iy);
            mc.func_175599_af().func_175030_a(mc.field_71466_p, stack, 31, (int) iy);
            RenderHelper.func_74518_a();
            GlStateManager.func_179121_F();
            GlStateManager.func_179140_f();

            if (stack.func_77984_f()) {
                int left = stack.func_77958_k() - stack.func_77952_i();
                float pct = left / (float) stack.func_77958_k();
                String dur = String.valueOf(left);
                float w = Fonts.width(dur, 0.9F, true);
                Fonts.drawC(dur, 22 - w, iy + 8,
                        RenderUtil.lerpColor(0xFFFF4757, 0xFF2ED573, pct), 0.9F, true, true);
            }
        }
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }

    // ---------- efeitos ----------

    private static void effects(El e, Minecraft mc) {
        Collection<PotionEffect> effects = mc.field_71439_g.func_70651_bq();
        List<String> names = new ArrayList<String>();
        List<String> times = new ArrayList<String>();
        for (PotionEffect effect : effects) {
            String name = I18n.func_135052_a(effect.func_76453_d());
            if (effect.func_76458_c() > 0) name += " " + (effect.func_76458_c() + 1);
            names.add(name);
            times.add(Potion.func_76389_a(effect));
        }
        if (names.isEmpty() && editing) {
            names.add("Velocidade II");
            times.add("0:30");
        }
        if (names.isEmpty()) return;
        rows(e, names, times, 0xFFFFFFFF, Config.accent());
    }

    // ---------- combate ----------

    private static void combo(El e, long now) {
        boolean active = combo > 0 && now - lastHitTime <= 2500L;
        if (!active && !editing) return;
        pill(e, "Combo", "x" + (active ? combo : 3));
    }

    private static void reach(El e, long now) {
        boolean active = reach > 0F && now - reachTime <= 4000L;
        if (!active && !editing) return;
        pill(e, "Reach", String.format("%.2f", active ? reach : 3.0F) + " blocos");
    }

    private static void tnt(El e, Minecraft mc) {
        int count = 0;
        int minFuse = Integer.MAX_VALUE;
        if (mc.field_71441_e != null) {
            for (Object o : mc.field_71441_e.field_72996_f) {
                if (o instanceof EntityTNTPrimed) {
                    count++;
                    int f = ((EntityTNTPrimed) o).field_70516_a;
                    if (f < minFuse) minFuse = f;
                }
            }
        }
        if (count == 0) {
            if (!editing) return;
            pill(e, "TNT", "x2 | 2.5s");
            return;
        }
        pill(e, "TNT", "x" + count + " | " + String.format("%.1f", Math.max(0, minFuse) / 20F) + "s");
    }

    private static void items(El e, Minecraft mc) {
        int arrows = 0, pearls = 0, apples = 0, potions = 0, blocks = 0;
        ItemStack[] inv = mc.field_71439_g.field_71071_by.field_70462_a;
        for (ItemStack st : inv) {
            if (st == null || st.func_77973_b() == null) continue;
            int n = st.field_77994_a;
            if ("item.arrow".equals(st.func_77977_a())) arrows += n;
            else if (st.func_77973_b() instanceof ItemEnderPearl) pearls += n;
            else if (st.func_77973_b() instanceof ItemAppleGold) apples += n;
            else if (st.func_77973_b() instanceof ItemPotion) potions += n;
            else if (st.func_77973_b() instanceof ItemBlock) blocks += n;
        }
        List<String> names = new ArrayList<String>();
        List<String> vals = new ArrayList<String>();
        if (arrows > 0) add(names, vals, "Flechas", arrows);
        if (pearls > 0) add(names, vals, "Perolas", pearls);
        if (apples > 0) add(names, vals, "Macas", apples);
        if (potions > 0) add(names, vals, "Pocoes", potions);
        if (blocks > 0) add(names, vals, "Blocos", blocks);
        if (names.isEmpty() && editing) {
            add(names, vals, "Flechas", 32);
            add(names, vals, "Perolas", 4);
            add(names, vals, "Blocos", 128);
        }
        if (names.isEmpty()) return;
        rows(e, names, vals, 0xFFB4B4BE, 0xFFFFFFFF);
    }

    private static void add(List<String> names, List<String> vals, String n, int v) {
        names.add(n);
        vals.add(String.valueOf(v));
    }

    /** Bordas vermelhas ao tomar dano. */
    private static void damageFlash(Minecraft mc, float sw, float sh) {
        int hurt = mc.field_71439_g.field_70737_aN;
        if (hurt <= 0) return;
        vignette(sw, sh, Math.min(1F, hurt / 10F) * 0.45F);
    }

    /** Borda vermelha pulsando quando a vida esta baixa (3 coracoes ou menos). */
    private static void lowHealth(Minecraft mc, float sw, float sh, long now) {
        float hp = mc.field_71439_g.func_110143_aJ();
        if (hp <= 0F || hp > 6F) return;
        float pulse = 0.5F + 0.5F * (float) Math.sin(now / 230.0);
        float urgency = 1F - (hp - 1F) / 6F;
        vignette(sw, sh, (0.12F + 0.22F * pulse) * (0.5F + 0.5F * Math.max(0F, urgency)));
    }

    private static void vignette(float sw, float sh, float base) {
        int layers = 8;
        float band = 6F;
        for (int i = 0; i < layers; i++) {
            int a = (int) (255F * base * (1F - i / (float) layers));
            int c = RenderUtil.withAlpha(0xFF2030, a);
            float o = i * band;
            RenderUtil.rect(o, o, sw - o, o + band, c);
            RenderUtil.rect(o, sh - o - band, sw - o, sh - o, c);
            RenderUtil.rect(o, o + band, o + band, sh - o - band, c);
            RenderUtil.rect(sw - o - band, o + band, sw - o, sh - o - band, c);
        }
    }
}
