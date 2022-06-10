package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

public interface GameService {

    SynchronousPlayer enrollToGame(String id, String player);

    Optional<GameDetails> quickGame(String player);

    SynchronousGame leaveGame(SynchronousPlayer player, String id);
}