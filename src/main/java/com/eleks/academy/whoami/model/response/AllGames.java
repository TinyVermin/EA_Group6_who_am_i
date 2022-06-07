package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllGames {
    private GameDetails gameDetails ;
    private List<SynchronousPlayer> players;

    public static AllGames of(SynchronousGame game){
        return AllGames.builder()
                .gameDetails(GameDetails.of(game))
                .players(game.getPlayersInGame())
                .build();
    }
}
