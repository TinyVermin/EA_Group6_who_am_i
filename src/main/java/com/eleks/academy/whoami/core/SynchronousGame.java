package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.model.response.PlayersWithState;

import java.util.List;
import java.util.Optional;

public interface SynchronousGame  {

    Optional<SynchronousPlayer> findPlayer(String player);

    String getId();

    SynchronousGame enrollToGame(SynchronousPlayer player);

    List<SynchronousPlayer> getPlayersInGame();

    List<PlayersWithState> getPlayersInGameWithState();

    GameState getStatus();

    boolean isAvailable();

    void setCharacter(String player, String character);

    SynchronousGame start();

    void askQuestion(SynchronousPlayer player, String message);
}