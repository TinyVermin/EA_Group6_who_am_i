package com.eleks.academy.whoami.core;

import java.util.concurrent.Future;

public interface Game {
     Future<GameState> play();
}