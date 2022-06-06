package com.eleks.academy.whoami.core;

import java.util.Optional;

public interface SynchronousGame extends Game{

    Optional<SynchronousPlayer> findPlayer(String player);

    String getId();

    SynchronousGame enrollToGame(SynchronousPlayer player);

    Integer getPlayersInGame();

    GameState getStatus();

    boolean isAvailable();

}