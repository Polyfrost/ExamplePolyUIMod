package org.polyfrost.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

import static net.minecraft.server.command.CommandManager.literal;

public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("examplemod").executes(context -> {
                try {
                    MinecraftClient.getInstance().submit(() -> {
                        MinecraftClient.getInstance().setScreen(new PolyUIScreen(ExampleUI.create()));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 1;
            }));
        });
    }
}
