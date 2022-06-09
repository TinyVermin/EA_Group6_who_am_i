package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.eleks.academy.whoami.utils.StringUtils.Headers.PLAYER;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/quick-game")
    public ResponseEntity<GameDetails> quickGame(@RequestHeader(PLAYER) String player) {
        return this.gameService.quickGame(player)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameDetails createGame(@RequestHeader(PLAYER) String player) {
        return this.gameService.createGame(player);
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<SynchronousPlayer> enrollToGame(@PathVariable("id") String id,
                                                          @RequestHeader(PLAYER) String player) {
        return this.gameService.enrollToGame(id, player)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.badRequest().build());
    }
}