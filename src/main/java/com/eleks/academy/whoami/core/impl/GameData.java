package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.eleks.academy.whoami.model.response.PlayerState.NOT_READY;

public class GameData {

    private final List<SynchronousPlayer> players = new ArrayList<>();
    private final Map<String, String> characterMap = new ConcurrentHashMap<>();
    private final Map<String, PlayerState> playersState = new ConcurrentHashMap<>();
    private final Map<String, Integer> inactivityCounter = new ConcurrentHashMap<>();
    @Getter
    private long initialTime;

    public void addPlayer(SynchronousPlayer player) {
        this.players.add(player);
        this.playersState.put(player.getId(), NOT_READY);
        this.inactivityCounter.put(player.getId(), 0);
    }

    public void setInitialTime() {
        this.initialTime = System.currentTimeMillis();
    }

    public void removePlayer(SynchronousPlayer player) {
        this.players.remove(player);
    }

    public List<SynchronousPlayer> allPlayers() {
        return this.players;
    }

    public int countPlayers() {
        return this.players.size();
    }

    public void putCharacter(String id, String character) {
        this.characterMap.put(id, character);
    }

    public boolean isContainCharacter(String id) {
        return this.characterMap.containsKey(id);
    }

    public void assignCharacters() {
        this.characterMap.forEach((playerId, character) -> players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .ifPresent(player -> player.setCharacter(character)));
    }

    public int characterMapSize() {
        return this.characterMap.size();
    }

    public int getInactivityCounter(String id) {
        return this.inactivityCounter.get(id);
    }

    public void incrementInactivityCounter(String id) {
        var counter = inactivityCounter.get(id);
        this.inactivityCounter.put(id, ++counter);
    }

    public void clearInactivityCounter(String id) {
        this.inactivityCounter.put(id, 0);
    }

    public void updatePlayerState(String id, PlayerState playerState) {
        this.playersState.put(id, playerState);
    }

    public void mixCharacters() {
        var characters = new ArrayList<>(characterMap.values());
        this.characterMap.keySet().forEach(key -> this.characterMap
                .computeIfPresent(key, (k, value) -> {
                    var randomCharacter = characters.stream()
                            .filter(character -> !value.equals(character))
                            .findAny()
                            .map(character -> {
                                var index = characters.indexOf(character);
                                return characters.remove(index);
                            });
                    return randomCharacter.orElse(value);
                }));
    }

    public void updateAllPlayersState(String currentTurnPlayerId) {
        this.playersState.put(currentTurnPlayerId, PlayerState.ASKING);
        this.playersState.keySet().stream()
                .filter(key -> !key.equals(currentTurnPlayerId))
                .forEach(key -> this.playersState.put(key, PlayerState.ANSWERING));
    }

    public PlayerState getPlayerState(String id) {
        return this.playersState.get(id);
    }
}
