package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Game;
import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.model.request.PlayersAnswer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayersWithState;
import org.springframework.http.HttpStatus;
import org.springframework.util.IdGenerator;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.eleks.academy.whoami.model.response.PlayerState.*;

public class PersistentGame implements SynchronousGame {

    private final Lock turnLock = new ReentrantLock();
    private final String id;
    private final Integer maxPlayers;
    private final GameDataImpl gameData;
    public static final long SUGGESTING_CHARACTER_TIMEOUT = 120;
    public static final long WAITING_QUESTION_TIMEOUT = 60;
    public static final long WAITING_ANSWER_TIMEOUT = 20;
    public static final String TIME_OVER = "Time is over";
    private static final String NOT_AVAILABLE = "Not available";

    private GameState state;

    public PersistentGame(String hostPlayer, Integer maxPlayers, IdGenerator uuid) {
        this.id = uuid.generateId().toString();
        this.maxPlayers = maxPlayers;
        state = GameState.WAITING_FOR_PLAYER;
        gameData = new GameDataImpl();
        var persistentPlayer = new PersistentPlayer(hostPlayer, uuid.generateId().toString());
        gameData.addPlayer(persistentPlayer);
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return gameData.allPlayers()
                .stream()
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
            gameData.allPlayers()
                    .stream()
                    .filter(newPlayer -> newPlayer.getName().equals(player.getName()))
                    .findFirst()
                    .ifPresent(m -> {
                        throw new GameException("Player already exist");
                    });
            if (gameData.allPlayers().size() < this.maxPlayers) {
                gameData.addPlayer(player);
                if (gameData.allPlayers().size() == maxPlayers) {
                    state = GameState.SUGGESTING_CHARACTER;
                    gameData.setInitialTime();
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
        return gameData.allPlayers();
    }

    @Override
    public List<PlayersWithState> getPlayersInGameWithState() {
        return gameData.allPlayers()
                .stream()
                .map(player -> PlayersWithState.builder()
                        .player(player)
                        .state(gameData.getPlayerState(player.getId()))
                        .build())
                .toList();
    }

    @Override
    public void setCharacter(String player, String character) {
        turnLock.lock();
        try {
            findPlayer(player)
                    .or(() -> {
                        throw new GameException("Player '" + player + "' is not found");
                    })
                    .filter(existingPlayer -> !gameData.isContainCharacter(existingPlayer.getId()))
                    .or(() -> {
                        throw new GameException("You already suggested character");
                    })
                    .filter(timer -> isTimeOut(gameData.getInitialTime(), SUGGESTING_CHARACTER_TIMEOUT))
                    .or(() -> {
                        state = GameState.FINISHED;
                        throw new GameException(TIME_OVER);
                    })
                    .ifPresent(synchronousPlayer -> {
                        gameData.putCharacter(synchronousPlayer.getId(), character);
                        gameData.updatePlayerState(synchronousPlayer.getId(), PlayerState.READY);
                        if (gameData.availableCharactersSize() == maxPlayers) {
                            state = GameState.READY_TO_START;
                        }
                    });
        } finally {
            turnLock.unlock();
        }
    }

    @Override
    public SynchronousGame start() {
        gameData.mixCharacters();
        gameData.assignCharacters();
        gameData.markAnsweringStateExceptCurrentTurnPlayer(gameData.allPlayers().get(0).getId());
        state = GameState.PROCESSING_QUESTION;

        CompletableFuture.supplyAsync(() -> {
            Game gameLoop = new GameLoop(gameData);
            return gameLoop.play();
        }).thenAccept(gameState -> state = ((CompletableFuture<GameState>) gameState).join());
        return this;
    }

    @Override
    public void askQuestion(SynchronousPlayer player, String message) {
        hasCorrectState(player, ASKING)
                .filter(timer -> isTimeOut(gameData.getInitialTime(), WAITING_QUESTION_TIMEOUT))
                .or(() -> {
                    gameData.updatePlayerState(player.getId(), LOSER);
                    throw new GameException(TIME_OVER);
                })
                .ifPresent(synchronousPlayer -> synchronousPlayer.setAnswer(Answer.builder()
                        .state(ASKING)
                        .message(message)
                        .build()));
    }

    @Override
    public void answerQuestion(SynchronousPlayer player, PlayersAnswer answer) {
        turnLock.lock();
        var playerId = player.getId();
        try {
            hasCorrectState(player, ANSWERING)
                    .filter(timer -> isTimeOut(gameData.getInitialTime(), WAITING_ANSWER_TIMEOUT))
                    .or(() -> {
                        var counter = gameData.getInactivityCounter(playerId);
                        if (counter == 3) {
                            gameData.updatePlayerState(playerId, LOSER);
                            return Optional.empty();
                        }
                        gameData.incrementInactivityCounter(playerId);
                        return Optional.of(player);
                    })
                    .ifPresent(synchronousPlayer -> {
                        synchronousPlayer.setAnswer(Answer.builder()
                                .state(ANSWERING)
                                .message(answer.toString())
                                .build());
                        gameData.clearInactivityCounter(playerId);
                    });
        } finally {
            turnLock.unlock();
        }
    }

    private Optional<SynchronousPlayer> hasCorrectState(SynchronousPlayer player, PlayerState playerState) {
        return Optional.of(gameData)
                .filter(status -> status.getPlayerState(player.getId()).equals(playerState))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .map(then -> player);
    }

    private boolean isTimeOut(long compareTime, long duration) {
        return (System.currentTimeMillis() - compareTime) <= TimeUnit.SECONDS.toMillis(duration);
    }
}
