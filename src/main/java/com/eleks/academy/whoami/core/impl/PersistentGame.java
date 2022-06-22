package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import org.springframework.http.HttpStatus;
import org.springframework.util.IdGenerator;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PersistentGame implements SynchronousGame {

    private final Lock turnLock = new ReentrantLock();
    private final String id;
    private final IdGenerator uuid;
    private final Integer maxPlayers;
    private final List<SynchronousPlayer> players = new ArrayList<>();

    private GameState state;

    public PersistentGame(String hostPlayer, Integer maxPlayers, IdGenerator uuid) {
        this.uuid = uuid;
        this.id = uuid.generateId().toString();
        this.maxPlayers = maxPlayers;
        state = GameState.WAITING_FOR_PLAYER;
        players.add(new PersistentPlayer(hostPlayer));
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return Optional.empty();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public SynchronousGame enrollToGame(SynchronousPlayer player) {
        turnLock.lock();
        try {
            players.stream()
                    .filter(f -> f.getName().equals(player.getName()))
                    .findFirst()
                    .ifPresent(m -> {
                        throw new GameException("Player already exist");
                    });
            if (players.size() < this.maxPlayers) {
                players.add(player);
                if (players.size() == maxPlayers) {
                    state = GameState.SUGGESTING_CHARACTER;
                }
                return this;
            }
        } finally {
            turnLock.unlock();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot enroll to a game");
    }

    @Override
    public boolean isAvailable() {
        return state == GameState.WAITING_FOR_PLAYER;
    }

    @Override
    public GameState getStatus() {
        return state;
    }

    @Override
    public Integer getPlayersInGame() {
        return players.size();
    }

    @Override
    public SynchronousGame leaveGame(String player) {
        turnLock.lock();
        try {
            if (isPreparingStage()) {
                players.clear();
                state = GameState.GAME_FINISHED;
                return this;
            } else {
                players.removeIf(p -> p.getName().equals(player));
                return this;
            }
        } finally {
            turnLock.unlock();
        }
    }

    private boolean isPreparingStage() {
        return state == GameState.WAITING_FOR_PLAYER || state == GameState.SUGGESTING_CHARACTER;
    }
}