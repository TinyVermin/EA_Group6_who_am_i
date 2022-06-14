package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayersWithState;
import org.springframework.http.HttpStatus;
import org.springframework.util.IdGenerator;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PersistentGame implements SynchronousGame {

    private final Lock turnLock = new ReentrantLock();
    private final String id;
    private final Integer maxPlayers;
    private final List<SynchronousPlayer> players = new ArrayList<>();
    private final Map<String, String> characterMap = new ConcurrentHashMap<>();
    private final Map<String, PlayerState> playersState = new ConcurrentHashMap<>();
    public static final long SUGGESTING_CHARACTER_TIME_OUT = 120;

    private GameState state;
    private long initialTime;

    public PersistentGame(String hostPlayer, Integer maxPlayers, IdGenerator uuid) {
        this.id = uuid.generateId().toString();
        this.maxPlayers = maxPlayers;
        state = GameState.WAITING_FOR_PLAYER;
        var persistentPlayer = new PersistentPlayer(hostPlayer, uuid.generateId().toString());
        players.add(persistentPlayer);
        playersState.put(persistentPlayer.getId(), PlayerState.NOT_READY);
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return players.stream()
                .filter(existingPlayer -> existingPlayer.getName().equals(player))
                .findFirst();
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
                    .filter(newPlayer -> newPlayer.getName().equals(player.getName()))
                    .findFirst()
                    .ifPresent(m -> {
                        throw new GameException("Player already exist");
                    });
            if (players.size() < this.maxPlayers) {
                players.add(player);
                playersState.put(player.getId(), PlayerState.NOT_READY);
                if (players.size() == maxPlayers) {
                    state = GameState.SUGGESTING_CHARACTER;
                    initialTime = System.currentTimeMillis();
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
    public List<SynchronousPlayer> getPlayersInGame() {
        return this.players;
    }

    @Override
    public List<PlayersWithState> getPlayersInGameWithState() {
        return players.stream()
                .map(player -> PlayersWithState.builder()
                        .player(player)
                        .state(playersState.getOrDefault(player.getId(), PlayerState.NOT_READY))
                        .build())
                .toList();
    }

    @Override
    public void setCharacter(String player, String character) {
        turnLock.lock();
        try {
            players.stream()
                    .filter(existingPlayer -> existingPlayer.getName().equals(player))
                    .findFirst()
                    .or(() -> {
                        throw new GameException("Player '" + player + "' is not found");
                    })
                    .filter(existingPlayer -> !characterMap.containsKey(existingPlayer.getId()))
                    .or(() -> {
                        throw new GameException("You already suggested character");
                    })
                    .filter(timer -> isTimeOut(initialTime, SUGGESTING_CHARACTER_TIME_OUT))
                    .or(() -> {
                        state = GameState.FINISHED;
                        throw new GameException("Time is out");
                    })
                    .ifPresent(synchronousPlayer -> {
                        synchronousPlayer.setCharacter(character);
                        characterMap.put(synchronousPlayer.getId(), character);
                        playersState.put(synchronousPlayer.getId(), PlayerState.READY);
                        if (characterMap.size() == maxPlayers) {
                            state = GameState.PROCESSING_QUESTION;
                        }
                    });
        } finally {
            turnLock.unlock();
        }
    }

    private boolean isTimeOut(long compareTime, long duration) {
        return (System.currentTimeMillis() - compareTime) <= TimeUnit.SECONDS.toMillis(duration);
    }
}