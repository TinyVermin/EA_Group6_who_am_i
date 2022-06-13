package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.GameDetails;
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

    @Override
    public Optional<SynchronousPlayer> enrollToGame(String id, String player) {
        SynchronousPlayer synchronousPlayer = this.gameRepository.findById(id)
                .filter(SynchronousGame::isAvailable)
                .map(game -> game.enrollToGame(new PersistentPlayer(player)))
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
                .map(game -> game.enrollToGame(new PersistentPlayer(player)))
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
                .filter(state -> state.getStatus() == GameState.SUGGESTING_CHARACTER)
                .or(() -> {
                    throw new GameException("Not available");
                })
                .ifPresentOrElse(p -> p.setCharacter(player, character),
                        () -> {
                            throw new GameException("Game is not found");
                        });
    }

    @Override
    public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
        return this.gameRepository.findById(id)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of);
    }

    @Override
    public Optional<SynchronousPlayer> renamePlayer(String id, String oldName, String newName) {
        return this.gameRepository.findById(id)
                .flatMap(game->game.findPlayer(oldName))
                .or(()->{throw  new GameException("Player '" + oldName + "' is not found");})
                .map(pl->pl.setName(newName));
    }
}