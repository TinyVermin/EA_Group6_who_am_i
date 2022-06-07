package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.AllGames;
import com.eleks.academy.whoami.model.response.GameDetails;

import java.util.List;
import java.util.Optional;

public interface GameService {

    SynchronousPlayer enrollToGame(String id, String player);

    Optional<GameDetails> quickGame(String player);

    List<AllGames> findAvailableGames(String player);
}