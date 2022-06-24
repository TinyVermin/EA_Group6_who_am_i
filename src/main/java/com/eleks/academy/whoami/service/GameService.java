package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;

import java.util.List;
import java.util.Optional;

public interface GameService {

    Optional<SynchronousPlayer> enrollToGame(String id, String player);

    Optional<GameDetails> quickGame(String player);

    Optional<GameDetails> createGame(String player, Integer maxPlayer);

    List<GameLight> findAvailableGames(String player);

    void suggestCharacter(String id, String player, String character);

    Optional<GameDetails> findById(String id);

    Optional<SynchronousPlayer> renamePlayer(String id, String oldName, String newName);

    Optional<GameDetails> startGame(String id, String player);

    void askQuestion(String id, String player, String message);

    void answerQuestion(String id, String player, String answer);
}
