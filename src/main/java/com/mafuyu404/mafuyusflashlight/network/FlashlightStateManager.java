package com.mafuyu404.mafuyusflashlight.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlashlightStateManager {
    private static final Map<UUID, Boolean> playerFlashlightStates = new ConcurrentHashMap<>();

    public static void setPlayerFlashlightState(UUID playerId, boolean isOn) {
        if (isOn) {
            playerFlashlightStates.put(playerId, true);
        } else {
            playerFlashlightStates.remove(playerId);
        }
    }

    public static boolean isPlayerUsingFlashlight(UUID playerId) {
        return playerFlashlightStates.getOrDefault(playerId, false);
    }

    public static Map<UUID, Boolean> getAllFlashlightStates() {
        return new ConcurrentHashMap<>(playerFlashlightStates);
    }

    public static void clearAll() {
        playerFlashlightStates.clear();
    }
}