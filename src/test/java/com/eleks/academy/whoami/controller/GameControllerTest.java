package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

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
                .andExpect(jsonPath("$.status").value(GameState.WAITING_FOR_PLAYER.toString()))
                .andExpect(jsonPath("$.players[0].state").value(PlayerState.NOT_READY.toString()));
    }

    @Test
    void doubleRequest_QuickGame_WithTheSamePlayer() throws Exception {
        var game = new PersistentGame("Pol", 4, uuidGenerator);
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
        var game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);
        game.enrollToGame(new PersistentPlayer("Sam", uuidGenerator.generateId().toString()));
        game.enrollToGame(new PersistentPlayer("Jack", uuidGenerator.generateId().toString()));
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/quick-game")
                                .header("X-Player", "Kat")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value(GameState.SUGGESTING_CHARACTER.toString()));

    }

    @Test
    void crateGame() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games")
                                .header("X-Player", "player")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(GameState.WAITING_FOR_PLAYER.toString()))
                .andExpect(jsonPath("$.players[0].player.name").value("player"))
                .andExpect(jsonPath("$.players[0].state").value(PlayerState.NOT_READY.toString()));
    }

    @Test
    void enrolToGame() throws Exception {
        var game = new PersistentGame("player", 4, uuidGenerator);
        gameRepository.save(game);

        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/players")
                                .header("X-Player", "player2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("player2"));
    }

    @Test
    void findAvailableGames() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games")
                                .header("X-Player", "player"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
    }

    @Test
    void suggestCharacter() throws Exception {
        var game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);
        game.enrollToGame(new PersistentPlayer("Sam", uuidGenerator.generateId().toString()));
        game.enrollToGame(new PersistentPlayer("Jack", uuidGenerator.generateId().toString()));
        game.enrollToGame(new PersistentPlayer("Kat", uuidGenerator.generateId().toString()));
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/characters")
                                .content("""
                                        {
                                          "character": "Batman"
                                        }""")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Player", "Pol"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
    }

    @Test
    void handelSuggestCharacterValidationError() throws Exception {
        var game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);
        game.enrollToGame(new PersistentPlayer("Sam", uuidGenerator.generateId().toString()));
        game.enrollToGame(new PersistentPlayer("Jack", uuidGenerator.generateId().toString()));
        game.enrollToGame(new PersistentPlayer("Kat", uuidGenerator.generateId().toString()));
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/characters")
                                .content("""
                                        {
                                          "character": "B"
                                        }""")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Player", "Pol"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed!"));
    }

    @Test
    void findById() throws Exception {
        var game = new PersistentGame("Pol", 4, uuidGenerator);
        gameRepository.save(game);
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/" + game.getId())
                                .header("X-Player", "Pol")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.status").value(GameState.WAITING_FOR_PLAYER.toString()))
                .andExpect(jsonPath("$.players[0].player.name").value("Pol"))
                .andExpect(jsonPath("$.players[0].state").value(PlayerState.NOT_READY.toString()));
    }

    @Test
    void renamePlayer() throws Exception {
        var game = new PersistentGame("Player", 4, uuidGenerator);
        gameRepository.save(game);
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/players/name")
                                .header("X-Player", "Player")
                                .param("name", "Pol")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pol"));
    }

    @Test
    void handelRequestParameterValidationError() throws Exception {
        var game = new PersistentGame("Player", 4, uuidGenerator);
        gameRepository.save(game);
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/" + game.getId() + "/players/name")
                                .header("X-Player", "Player")
                                .param("name", "P")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed!"));
    }
}