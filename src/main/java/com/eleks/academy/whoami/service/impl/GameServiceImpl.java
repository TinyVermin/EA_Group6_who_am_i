package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final IdGenerator uuidGenerator;
    public static final String GAME_NOT_FOUND = "Game is not found";
    public static final String NOT_AVAILABLE = "Not available";

    @Override
    public Optional<SynchronousPlayer> enrollToGame(String id, String player) {
        SynchronousPlayer synchronousPlayer = this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(SynchronousGame::isAvailable)
                .map(game -> game.enrollToGame(new PersistentPlayer(player, uuidGenerator.generateId().toString())))
                .flatMap(m -> m.findPlayer(player))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot enroll to a game")
                );
        return Optional.of(synchronousPlayer);
    }

    @Override
    public Optional<GameDetails> quickGame(String player) {
        var synchronousGame = this.gameRepository.findAllAvailable(player)
                .findFirst()
                .map(game -> game.enrollToGame(new PersistentPlayer(player, uuidGenerator.generateId().toString())))
                .orElseGet(() -> this.gameRepository.save(new PersistentGame(player, 4, uuidGenerator)));

        var gameDetails = GameDetails.of(synchronousGame);
        return Optional.of(gameDetails);
    }

    @Override
    public Optional<GameDetails> createGame(String player) {
        var game = this.gameRepository.save(new PersistentGame(player, 4, uuidGenerator));
        return Optional.of(GameDetails.of(game));
    }

    @Override
    public List<GameLight> findAvailableGames(String player) {
        return this.gameRepository.findAllAvailable(player)
                .map(GameLight::of)
                .toList();
    }

    @Override
    public void suggestCharacter(String id, String player, String character) {
        this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(state -> state.getStatus() == GameState.SUGGESTING_CHARACTER)
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .ifPresent(game -> game.setCharacter(player, character));
    }

    @Override
    public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
        return this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of);
    }

    @Override
    public Optional<SynchronousPlayer> renamePlayer(String id, String oldName, String newName) {
        var currentGame = this.gameRepository.findById(id);
        var synchronousPlayer = currentGame
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .map(game -> game.findPlayer(oldName))
                .orElseThrow(() -> new GameException("Player '" + oldName + "' is not found"));
        currentGame
                .map(SynchronousGame::getPlayersInGame)
                .ifPresent(players -> players.stream()
                        .filter(f -> !f.getName().equals(newName))
                        .findFirst()
                        .orElseThrow(() -> new GameException("Player with name '" + newName + "' already exist")
                        ));
        return synchronousPlayer
                .map(player -> player.setName(newName));
    }


    @Override
    public Optional<GameDetails> startGame(String id, String player) {
        return this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(game -> game.getStatus().equals(GameState.READY_TO_START))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .map(SynchronousGame::start)
                .map(GameDetails::of);
    }

    @Override
    public void askQuestion(String id, String player, String message) {
        var currentGame = this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(game -> game.getStatus().equals(GameState.PROCESSING_QUESTION))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                });
        var currentPlayer = currentGame
                .flatMap(game -> game.findPlayer(player))
                .orElseThrow(() -> {
                    throw new GameException("Player '" + player + "' is not found");
                });
        currentGame.ifPresent(game -> game.askQuestion(currentPlayer, message));
    }
}