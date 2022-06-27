package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.*;
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

    private static final int MAX_NUMBER_COUNT_MISSING_ANSWER = 3;
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
            if(this.isFinished()){
                break;
            }
            while (turnResult) {
                turnResult = this.makeTurn();
            }
            var synchronousPlayer = turn.changeTurn();
            gameData.markAnsweringStateExceptCurrentTurnPlayer(synchronousPlayer.getId());
            status = !this.isFinished();
        }
        return CompletableFuture.completedFuture(GameState.FINISHED);
    }

    private boolean isFinished() {
        return gameData.allPlayers().size() < 2;
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

        gameData.addPlayerQuestionInHistory(currentGuesser.getName(), question);
        gameData.setInitialTime();
        var answers = turn.getOtherPlayers()
                .parallelStream()
                .map(player -> player.getCurrentAnswer(WAITING_ANSWER_TIMEOUT, TimeUnit.SECONDS)
                        .handle((message, exception) -> {
                            var playerId = player.getId();
                            if (exception != null) {
                                gameData.savePlayersAnswer(player.getName(), PlayersAnswer.NOT_SURE);
                                gameData.incrementInactivityCounter(playerId);
                                if (gameData.getInactivityCounter(playerId) == MAX_NUMBER_COUNT_MISSING_ANSWER) {
                                    gameData.removePlayer(player);
                                    gameData.updatePlayerState(playerId, LOSER);
                                }
                                return PlayersAnswer.NOT_SURE;
                            }
                            gameData.clearInactivityCounter(playerId);
                            gameData.savePlayersAnswer(player.getName(), message);
                            return message;
                        }).join()
                ).toList();

        Predicate<PlayersAnswer> yes = YES::equals;
        Predicate<PlayersAnswer> notSure = NOT_SURE::equals;
        long positiveAnswer = answers.stream().filter(yes.or(notSure)).count();
        long negativeAnswer = answers.stream().filter(NO::equals).count();
        gameData.addPlayerAnswersInHistory();

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
