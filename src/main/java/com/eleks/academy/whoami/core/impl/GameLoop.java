package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Game;
import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class GameLoop implements Game {
    private final List<SynchronousPlayer> players;
    private final Map<String, String> characterMap;


    public GameLoop(List<SynchronousPlayer> players, Map<String, String> characterMap) {
        this.players = players;
        this.characterMap = characterMap;
    }

    @Override
    public Future<GameState> play() {
        return null;
    }
}
