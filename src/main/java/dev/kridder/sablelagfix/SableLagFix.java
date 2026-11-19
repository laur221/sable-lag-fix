package dev.kridder.sablelagfix;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod("sablelagfix")
public class SableLagFix {
    public static final String MOD_ID = "sablelagfix";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SableLagFix(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SableLagFixConfig.SPEC);
        LOGGER.info("Sable Lag Fix loaded");
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class Events {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("sablelagfix")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.literal("stats").executes(ctx -> {
                        String s = Stats.summary();
                        ctx.getSource().sendSuccess(() -> Component.literal("Sable Lag Fix: " + s), false);
                        return 1;
                    }))
                    .then(Commands.literal("reset").executes(ctx -> {
                        Stats.reset();
                        ctx.getSource().sendSuccess(() -> Component.literal("Sable Lag Fix: stats reset"), false);
                        return 1;
                    })));
        }
    }
}