package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.model.response.PlayersWithState;

import java.util.List;

public interface Game {
    List<PlayersWithState> getPlayersInGame();
}