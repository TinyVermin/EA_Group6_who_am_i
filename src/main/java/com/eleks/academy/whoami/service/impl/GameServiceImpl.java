package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final IdGenerator uuidGenerator;

    @Override
    public SynchronousPlayer enrollToGame(String id, String player) {
        return this.gameRepository.findById(id)
                .filter(SynchronousGame::isAvailable)
                .map(game -> game.enrollToGame(new PersistentPlayer(player)))
                .flatMap(m -> m.findPlayer(player))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot enroll to a game")
                );
    }

    @Override
    public Optional<GameDetails> quickGame(String player) {
        var synchronousGame = this.gameRepository.findAllAvailable(player)
                .findFirst()
                .map(game -> game.enrollToGame(new PersistentPlayer(player)))
                .orElseGet(() -> this.gameRepository.save(new PersistentGame(player, 4, uuidGenerator)));

        var gameDetails = GameDetails.of(synchronousGame);
        return Optional.of(gameDetails);
    }


    
    @Override
    public SynchronousGame leaveGame(SynchronousPlayer player, String id) {    
        return this.gameRepository.findById(id)
        .filter(SynchronousGame::isAvailable)
        .map(game -> game.leaveGame(player))
        .flatMap(m -> m.findPlayer(player.getName()))      
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Game finished. Player has left game")
        );

    }

}