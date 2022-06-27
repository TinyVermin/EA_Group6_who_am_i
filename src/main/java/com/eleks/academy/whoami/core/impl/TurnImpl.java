package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.Turn;

import java.util.List;

public class TurnImpl implements Turn {
    private final List<SynchronousPlayer> players;
    private int currentPlayerIndex = 0;

    public TurnImpl(List<SynchronousPlayer> players) {
        this.players = players;
    }

    @Override
    public SynchronousPlayer getGuesser() {
        return this.players.get(currentPlayerIndex);
    }

    @Override
    public List<SynchronousPlayer> getOtherPlayers() {
        return this.players.stream()
                .filter(player -> !player.getName().equals(this.getGuesser().getName()))
                .toList();
    }

    @Override
    public SynchronousPlayer changeTurn() {
        this.currentPlayerIndex = this.currentPlayerIndex + 1 >= this.players.size() ? 0 : this.currentPlayerIndex + 1;
        return players.get(currentPlayerIndex);
    }

}
