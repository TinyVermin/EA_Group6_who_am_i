package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.Game;
import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    GameRepository gameRepository;
    IdGenerator uuidGenerator;
    SynchronousGame game;
    String gameId;

    @BeforeEach
    void init() {
        uuidGenerator = new SimpleIdGenerator();
    }

    @Test
    void quickGame() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/quick-game")
                                .header("X-Player", "player")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value(GameState.WAITING_FOR_PLAYER.toString()))
                .andExpect(jsonPath("$.playersInGame").value(1));
    }

    @Test
    void doubleRequest_QuickGame_WithTheSamePlayer() throws Exception {
        game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);

        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/quick-game")
                                .header("X-Player", "Pol")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Player already exist"));
    }

    @Test
    void changeStatusAfterAddedLastPlayers_quickGame() throws Exception {
        game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);
        game.enrollToGame(new PersistentPlayer("Sam"));
        game.enrollToGame(new PersistentPlayer("Jack"));
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/quick-game")
                                .header("X-Player", "Kat")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value(GameState.SUGGESTING_CHARACTER.toString()))
                .andExpect(jsonPath("$.playersInGame").value(4));
    }
    @Test
    void crateGame() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games")
                                .header("X-Player", "player")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value(GameState.WAITING_FOR_PLAYER.toString()))
                .andExpect(jsonPath("$.playersInGame").value(1));
    }
    @Test

    void enrolToGame() throws Exception {
        var game = new PersistentGame("player",4,uuidGenerator);
        gameRepository.save(game);

        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/players")
                                .header("X-Player", "player2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("player2"));

    }
}