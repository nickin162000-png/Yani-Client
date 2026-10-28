package com.yani.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "yaniclient", name = "Yani Client", version = "1.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class YaniClient {

    private static final String CATEGORY = "Yani Client";

    private KeyBinding menuKey;
    private KeyBinding zoomKey;
    private KeyBinding sneakKey;
    private KeyBinding hudKey;

    private final Minecraft mc = Minecraft.func_71410_x();

    private float oldGamma = 1.0F;
    private boolean fullbrightApplied = false;
    private boolean zooming = false;
    private boolean oldSmooth = false;

    private float zoomCur = 1F;
    private float zoomGoal = 0.25F;
    private long lastFov = 0L;
    private boolean sneakLatched = false;
    private boolean sneakHeld = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Config.load();
        Hud.init();
        menuKey = new KeyBinding("Abrir menu", Keyboard.KEY_RSHIFT, CATEGORY);
        zoomKey = new KeyBinding("Zoom", Keyboard.KEY_C, CATEGORY);
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        sneakKey = new KeyBinding("Alternar agachar", Keyboard.KEY_Z, CATEGORY);
        hudKey = new KeyBinding("Mostrar/esconder HUD", Keyboard.KEY_H, CATEGORY);
        ClientRegistry.registerKeyBinding(sneakKey);
        ClientRegistry.registerKeyBinding(hudKey);
        zoomGoal = clampZoom(Config.getFloat("zoom.factor", 0.25F));
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ---------- Entrada ----------

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.buttonstate) Hud.click(e.button);
        // scroll muda a forca do zoom (e nao troca o item da hotbar)
        if (zooming && Config.get("Zoom Scroll") && e.dwheel != 0) {
            float step = e.dwheel > 0 ? 0.8F : 1.25F;
            zoomGoal = clampZoom(zoomGoal * step);
            Config.setFloat("zoom.factor", zoomGoal);
            e.setCanceled(true);
        }
    }

    private static float clampZoom(float v) {
        return Math.max(0.06F, Math.min(0.7F, v));
    }

    /** Contagem de combo e reach: so conta os golpes do proprio jogador. */
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (mc.field_71439_g != null && e.entity == mc.field_71439_g) Hud.hit(mc, e.target);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (menuKey.func_151468_f()) {
            mc.func_147108_a(new ModMenu());
        }
        if (sneakKey.func_151468_f() && Config.get("Toggle Sneak") && mc.field_71439_g != null) {
            sneakLatched = !sneakLatched;
            Toast.show("Toggle Sneak: " + (sneakLatched ? "LIGADO" : "DESLIGADO"));
        }
        if (hudKey.func_151468_f()) {
            Hud.hidden = !Hud.hidden;
            Toast.show("HUD: " + (Hud.hidden ? "ESCONDIDO" : "VISIVEL"));
        }
    }

    // ---------- Menus ----------

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (!Config.get("Menus Modernos")) return;
        if (e.gui instanceof GuiMainMenu) {
            e.gui = new ModernMainMenu();
        } else if (e.gui instanceof GuiIngameMenu && mc.field_71441_e != null) {
            e.gui = new ModernPauseMenu();
        }
    }

    // ---------- Tick ----------

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Hud.tick(mc);
        if (mc.field_71439_g == null) return;

        if (Config.get("Toggle Sprint")
                && mc.field_71474_y.field_74351_w.func_151470_d()
                && !mc.field_71439_g.func_70093_af()
                && !mc.field_71439_g.func_71039_bw()) {
            KeyBinding.func_74510_a(mc.field_71474_y.field_151444_V.func_151463_i(), true);
        }

        if (Config.get("Fullbright")) {
            if (!fullbrightApplied) {
                oldGamma = mc.field_71474_y.field_74333_Y;
                fullbrightApplied = true;
            }
            mc.field_71474_y.field_74333_Y = 100.0F;
        } else if (fullbrightApplied) {
            mc.field_71474_y.field_74333_Y = oldGamma;
            fullbrightApplied = false;
        }

        // agachar travado (toggle): mantem a tecla de agachar pressionada
        int sneakCode = mc.field_71474_y.field_74311_E.func_151463_i();
        boolean wantSneak = Config.get("Toggle Sneak") && sneakLatched;
        if (wantSneak) {
            KeyBinding.func_74510_a(sneakCode, true);
            sneakHeld = true;
        } else if (sneakHeld) {
            if (!Keyboard.isKeyDown(sneakCode)) KeyBinding.func_74510_a(sneakCode, false);
            sneakHeld = false;
            sneakLatched = false;
        }

        boolean wantZoom = Config.get("Zoom") && zoomKey.func_151470_d() && mc.field_71462_r == null;
        if (wantZoom && !zooming) {
            oldSmooth = mc.field_71474_y.field_74326_T;
            mc.field_71474_y.field_74326_T = true;
            zooming = true;
        } else if (!wantZoom && zooming) {
            mc.field_71474_y.field_74326_T = oldSmooth;
            zooming = false;
        }
    }

    @SubscribeEvent
    public void onFov(FOVUpdateEvent e) {
        long now = System.nanoTime();
        float dt = lastFov == 0L ? 0.016F : Math.min(0.1F, (now - lastFov) / 1.0E9F);
        lastFov = now;
        float goal = zooming ? zoomGoal : 1F;
        zoomCur += (goal - zoomCur) * (1F - (float) Math.exp(-dt * 14F));
        if (!zooming && Math.abs(zoomCur - 1F) < 0.004F) zoomCur = 1F;
        if (zoomCur < 0.999F) {
            e.newfov = e.fov * zoomCur;
        }
    }

    // ---------- Interface do jogo (substitui a padrao) ----------

    @SubscribeEvent
    public void onPre(RenderGameOverlayEvent.Pre e) {
        if (mc.field_71439_g == null) return;
        ScaledResolution sr = e.resolution;

        switch (e.type) {
            case HOTBAR:
                if (Config.get("Hotbar Moderna") && !mc.field_71442_b.func_78747_a()) {
                    e.setCanceled(true);
                    GameUi.hotbar(mc, sr);
                }
                break;
            case HEALTH:
                if (Config.get("Barras de Status")) {
                    e.setCanceled(true);
                    GameUi.status(mc, sr);
                }
                break;
            case ARMOR:
            case FOOD:
            case AIR:
            case EXPERIENCE:
                if (Config.get("Barras de Status")) e.setCanceled(true);
                break;
            case CROSSHAIRS:
                if (Config.get("Mira Moderna") && !mc.field_71474_y.field_74330_P) {
                    e.setCanceled(true);
                    if (mc.field_71474_y.field_74320_O == 0) GameUi.crosshair(sr);
                }
                break;
            default:
                break;
        }
    }

    // ---------- HUD ----------

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.field_71439_g == null || mc.field_71474_y.field_74330_P) return;

        Hud.render(mc, e.resolution);
        GlStateManager.func_179131_c(1F, 1F, 1F, 1F);
    }
}
