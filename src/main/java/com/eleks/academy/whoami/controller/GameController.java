package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.model.response.AllGames;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public List<AllGames> findAvailableGames(@RequestHeader(PLAYER) String player) {
        return this.gameService.findAvailableGames(player);
    }

}