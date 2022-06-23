package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.PlayersAnswer;
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
        SynchronousPlayer synchronousPlayer = this.findGame(id)
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
        this.findGame(id)
                .filter(state -> state.getStatus() == GameState.SUGGESTING_CHARACTER)
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .ifPresent(game -> game.setCharacter(player, character));
    }

    @Override
    public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
        return this.findGame(id)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of);
    }

    @Override
    public Optional<SynchronousPlayer> renamePlayer(String id, String oldName, String newName) {
        var currentGame = this.findGame(id);
        var synchronousPlayer = findPlayer(id, oldName);
        currentGame
                .filter(game -> game.getStatus().equals(GameState.SUGGESTING_CHARACTER))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .map(SynchronousGame::getPlayersInGame)
                .ifPresent(players -> players.stream()
                        .filter(f -> !f.getName().equals(newName))
                        .findFirst()
                        .orElseThrow(() -> new GameException("Player with name '" + newName + "' already exist")
                        ));
        return Optional.of(synchronousPlayer)
                .map(player -> player.setName(newName));
    }


    @Override
    public Optional<GameDetails> startGame(String id, String player) {
        return this.findGame(id)
                .filter(game -> game.getStatus().equals(GameState.READY_TO_START))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .map(SynchronousGame::start)
                .map(GameDetails::of);
    }

    @Override
    public void askQuestion(String id, String player, String message) {
        var currentGame = this.findGame(id);
        var currentPlayer = findPlayer(id, player);
        currentGame
                .filter(game -> game.getStatus().equals(GameState.PROCESSING_QUESTION))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                })
                .ifPresent(game -> game.askQuestion(currentPlayer, message));
    }

    @Override
    public void answerQuestion(String id, String player, String answer) {
        var currentGame = this.findGame(id);
        var currentPlayer = findPlayer(id, player);
        try {
            currentGame
                    .filter(game -> game.getStatus().equals(GameState.PROCESSING_QUESTION))
                    .or(() -> {
                        throw new GameException(NOT_AVAILABLE);
                    })
                    .ifPresent(game -> game.answerQuestion(currentPlayer, PlayersAnswer.valueOf(answer.toUpperCase())));
        } catch (IllegalArgumentException e) {
            throw new GameException("Value '" + answer + "' is not correct. Use: yes, no, not_sure");
        }
    }

    private Optional<SynchronousGame> findGame(String id) {
        return this.gameRepository.findById(id)
                .or(() -> {
                    throw new GameException(GAME_NOT_FOUND);
                })
                .filter(game -> game.getStatus().equals(GameState.PROCESSING_QUESTION))
                .or(() -> {
                    throw new GameException(NOT_AVAILABLE);
                });
    }

    private SynchronousPlayer findPlayer(String id, String player) {
        return this.findGame(id)
                .flatMap(game -> game.findPlayer(player))
                .orElseThrow(() -> {
                    throw new GameException("Player '" + player + "' is not found");
                });
    }
}
