package com.eleks.academy.whoami.controller;


import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.Message;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.eleks.academy.whoami.utils.StringUtils.Headers.PLAYER;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Validated
public class GameController {

  private final GameService gameService;

    @GetMapping("/quick-game")
    public ResponseEntity<GameDetails> quickGame(@RequestHeader(PLAYER) String player) {
        return this.gameService.quickGame(player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping
    public ResponseEntity<GameDetails> createGame(@RequestHeader(PLAYER) String player,
                                                  @RequestBody NewGameRequest gameRequest) {
        return this.gameService.createGame(player, gameRequest.getMaxPlayers())
                .map(gameDetails -> ResponseEntity.status(HttpStatus.CREATED).body(gameDetails))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public List<GameLight> findAvailableGames(@RequestHeader(PLAYER) String player) {
        return this.gameService.findAvailableGames(player);
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<SynchronousPlayer> enrollToGame(@PathVariable("id") String id,
                                                          @RequestHeader(PLAYER) String player) {
        return this.gameService.enrollToGame(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/characters")
    @ResponseStatus(HttpStatus.OK)
    public void suggestCharacter(@PathVariable("id") String id,
                                 @RequestHeader(PLAYER) String player,
                                 @Valid @RequestBody CharacterSuggestion suggestion) {
        this.gameService.suggestCharacter(id, player, suggestion.getCharacter());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDetails> findById(@PathVariable("id") String id,
                                                @RequestHeader(PLAYER) String player) {
        return this.gameService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/players/name")
    public ResponseEntity<SynchronousPlayer> renamePlayer(@PathVariable("id") String id,
                                                          @RequestHeader(PLAYER) String player,
                                                          @RequestParam(value = "name") @Length(min = 2, max = 50) String newName) {
        return this.gameService.renamePlayer(id, player, newName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}")
    public ResponseEntity<GameDetails> startGame(@PathVariable("id") String id,
                                                 @RequestHeader(PLAYER) String player) {
        return this.gameService.startGame(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.OK)
    public void askQuestion(@PathVariable("id") String id,
                            @RequestHeader(PLAYER) String player,
                            @Valid @RequestBody Message message) {

        this.gameService.askQuestion(id, player, message.getMessage());
    }

  @GetMapping("/{id}/leave-game")
  public ResponseEntity<GameDetails> leaveGame(@RequestHeader(PLAYER) String player, @PathVariable("id") String id) {
    return this.gameService.leaveGame(player, id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }

    @PostMapping("/{id}/answer")
    @ResponseStatus(HttpStatus.OK)
    public void answerQuestion(@PathVariable("id") String id,
                               @RequestHeader(PLAYER) String player,
                               @RequestBody Message message) {
        this.gameService.answerQuestion(id, player, message.getMessage());
    }    
}