package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.eleks.academy.whoami.utils.StringUtils.Headers.PLAYER;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

  @GetMapping("/quick-game")
    public ResponseEntity<GameDetails> quickGame(@RequestHeader(PLAYER) String player){
     return this.gameService.quickGame(player)
             .map(ResponseEntity::ok)
             .orElseGet(()->ResponseEntity.badRequest().build());
  }

  @GetMapping("/{id}/leave-game")
    public ResponseEntity<GameDetails> leaveGame(@RequestHeader(PLAYER) SynchronousPlayer player, @PathVariable ("id") String id){
     return (ResponseEntity<GameDetails>) this.gameService.leaveGame(player, id);
  }

}