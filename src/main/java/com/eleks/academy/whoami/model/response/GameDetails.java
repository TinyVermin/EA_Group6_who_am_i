package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.GameState;
import com.eleks.academy.whoami.core.History;
import com.eleks.academy.whoami.core.SynchronousGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDetails {

    private String id;

    private GameState status;

    private List<PlayersWithState> players;

    private History history;

    public static GameDetails of(SynchronousGame game) {
        return GameDetails.builder()
                .id(game.getId())
                .status(game.getStatus())
                .players(game.getPlayersInGameWithState())
                .history(game.getHistory())
                .build();
    }

}