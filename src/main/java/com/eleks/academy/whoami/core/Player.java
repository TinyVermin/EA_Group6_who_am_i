package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.model.request.PlayersAnswer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface Player {

    String getId();

    CompletableFuture<String> getCurrentQuestion(long limit, TimeUnit unit);

    CompletableFuture<PlayersAnswer> getCurrentAnswer(long limit, TimeUnit unit);

}