package com.eleks.academy.whoami.core;

import java.util.List;

public interface Turn {
    SynchronousPlayer getGuesser();

    List<SynchronousPlayer> getOtherPlayers();

    void changeTurn();

}
