package com.eleks.academy.whoami.core;

import java.util.List;
import java.util.Optional;

public interface SynchronousGame extends Game {

    Optional<SynchronousPlayer> findPlayer(String player);

    String getId();

    SynchronousGame enrollToGame(SynchronousPlayer player);

    Integer getCountPlayersInGame();

    GameState getStatus();

    boolean isAvailable();

    void setCharacter(String player, String character);

}