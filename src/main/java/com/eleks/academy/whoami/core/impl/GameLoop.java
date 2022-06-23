package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Game;
import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.Turn;
import com.eleks.academy.whoami.model.request.PlayersAnswer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.eleks.academy.whoami.core.impl.PersistentGame.WAITING_ANSWER_TIMEOUT;
import static com.eleks.academy.whoami.core.impl.PersistentGame.WAITING_QUESTION_TIMEOUT;
import static com.eleks.academy.whoami.model.request.PlayersAnswer.*;
import static com.eleks.academy.whoami.model.response.PlayerState.*;

public class GameLoop implements Game {

    private final Turn turn;
    private final GameData gameData;
    public static final String MISSING_QUESTION = "";


    public GameLoop(GameData gameData) {
        this.gameData = gameData;
        turn = new TurnImpl(gameData.allPlayers());
    }

    @Override
    public Future<GameState> play() {
        boolean status = true;
        while (status) {
            boolean turnResult = this.makeTurn();

            while (turnResult) {
                turnResult = this.makeTurn();
            }
            turn.changeTurn();
            gameData.updateAllPlayersState(turn.getGuesser().getId());
            status = !this.isFinished();
        }
        return CompletableFuture.completedFuture(GameState.FINISHED);
    }

    private boolean isFinished() {
        return gameData.countPlayers() < 2;
    }

    private boolean makeTurn() {
        gameData.setInitialTime();
        var currentGuesser = turn.getGuesser();
        var question = currentGuesser.getCurrentQuestion(WAITING_QUESTION_TIMEOUT, TimeUnit.SECONDS)
                .handle((message, exception) -> {
                    if (exception != null) {
                        return MISSING_QUESTION;
                    }
                    return message;
                }).join();
        if (question.equals(MISSING_QUESTION)) {
            gameData.updatePlayerState(currentGuesser.getId(), LOSER);
            gameData.removePlayer(currentGuesser);
            return false;
        }
        gameData.setInitialTime();
        var answers = turn.getOtherPlayers()
                .parallelStream()
                .map(player -> player.getCurrentAnswer(WAITING_ANSWER_TIMEOUT, TimeUnit.SECONDS)
                        .handle((message, exception) -> {
                            if (exception != null) {
                                return PlayersAnswer.NOT_SURE;
                            }
                            return message;
                        }).join()
                ).toList();
        Predicate<PlayersAnswer> yes = YES::equals;
        Predicate<PlayersAnswer> notSure = NOT_SURE::equals;
        long positiveAnswer = answers.stream().filter(yes.or(notSure)).count();
        long negativeAnswer = answers.stream().filter(NO::equals).count();

        boolean win = positiveAnswer > negativeAnswer;
        if (win) {
            if (currentGuesser.isGuessing()) {
                gameData.updatePlayerState(currentGuesser.getId(), WINNER);
                gameData.removePlayer(currentGuesser);
            }
            return true;
        }
        return false;
    }

}
