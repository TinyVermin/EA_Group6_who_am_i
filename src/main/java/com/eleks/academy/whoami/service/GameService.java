package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;

import java.util.Optional;

public interface GameService {

    Optional<SynchronousPlayer> enrollToGame(String id, String player);

    Optional<GameDetails> quickGame(String player);

    GameDetails createGame(String player);
}