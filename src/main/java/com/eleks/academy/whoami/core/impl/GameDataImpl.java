package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.GameData;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.eleks.academy.whoami.model.response.PlayerState.*;

public class GameDataImpl implements GameData {

    private final List<SynchronousPlayer> players = new ArrayList<>();
    private final Map<String, String> characterMap = new ConcurrentHashMap<>();
    private final Map<String, PlayerState> playersState = new ConcurrentHashMap<>();
    private final Map<String, Integer> inactivityCounter = new ConcurrentHashMap<>();
    @Getter
    private long initialTime;

    @Override
    public void addPlayer(SynchronousPlayer player) {
        this.players.add(player);
        this.playersState.put(player.getId(), NOT_READY);
        this.inactivityCounter.put(player.getId(), 0);
    }

    @Override
    public void setInitialTime() {
        this.initialTime = System.currentTimeMillis();
    }

    @Override
    public void removePlayer(SynchronousPlayer player) {
        this.players.remove(player);
    }

    @Override
    public void removeAllPlayers() {
        this.players.clear();
    }

    @Override
    public List<SynchronousPlayer> allPlayers() {
        return this.players;
    }

    @Override
    public void putCharacter(String id, String character) {
        this.characterMap.put(id, character);
    }

    @Override
    public boolean isContainCharacter(String id) {
        return this.characterMap.containsKey(id);
    }

    @Override
    public void assignCharacters() {
        this.characterMap.forEach((playerId, character) -> players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .ifPresent(player -> player.setCharacter(character)));
    }

    @Override
    public int availableCharactersSize() {
        return this.characterMap.size();
    }

    @Override
    public int getInactivityCounter(String id) {
        return this.inactivityCounter.get(id);
    }

    @Override
    public void incrementInactivityCounter(String id) {
        var counter = inactivityCounter.get(id);
        this.inactivityCounter.put(id, ++counter);
    }

    @Override
    public void clearInactivityCounter(String id) {
        this.inactivityCounter.put(id, 0);
    }

    @Override
    public void updatePlayerState(String id, PlayerState playerState) {
        this.playersState.put(id, playerState);
    }

    @Override
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

    @Override
    public void markAnsweringStateExceptCurrentTurnPlayer(String currentTurnPlayerId) {
        updatePlayerState(currentTurnPlayerId, ASKING);
        allPlayers().stream()
                .filter(player -> !player.getId().equals(currentTurnPlayerId))
                .forEach(player -> updatePlayerState(player.getId(), ANSWERING));
    }

    @Override
    public PlayerState getPlayerState(String id) {
        return this.playersState.get(id);
    }
}
