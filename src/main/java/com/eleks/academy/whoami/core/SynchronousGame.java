package com.eleks.academy.whoami.core;

import java.util.List;
import java.util.Optional;

import com.eleks.academy.whoami.model.response.GameDetails;

public interface SynchronousGame extends Game{

    Optional<SynchronousPlayer> findPlayer(String player);

    String getId();

    SynchronousGame enrollToGame(SynchronousPlayer player);

    Integer getPlayersInGame();

    GameState getStatus();

    boolean isAvailable();

    SynchronousGame leaveGame(SynchronousPlayer player);

    boolean isFinished();

    boolean isNotFinished();

}