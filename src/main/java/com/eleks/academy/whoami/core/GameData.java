package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.model.response.PlayerState;

import java.util.List;

public interface GameData {

    void addPlayer(SynchronousPlayer player);

    void setInitialTime();

    long getInitialTime();

    void removePlayer(SynchronousPlayer player);

    List<SynchronousPlayer> allPlayers();

    void putCharacter(String id, String character);

    boolean isContainCharacter(String id);

    void assignCharacters();

    int availableCharactersSize();

    int getInactivityCounter(String id);

    void incrementInactivityCounter(String id);

    void clearInactivityCounter(String id);

    void updatePlayerState(String id, PlayerState playerState);

    void markAnsweringStateExceptCurrentTurnPlayer(String currentTurnPlayerId);

    PlayerState getPlayerState(String id);

    void mixCharacters();

}
