package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.SynchronousGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDetails {

    private String gameId;

    private GameState status;

    private Integer playersInGame;

    public static GameDetails of(SynchronousGame game) {
        return GameDetails.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .playersInGame(game.getCountPlayersInGame())
                .build();
    }

}