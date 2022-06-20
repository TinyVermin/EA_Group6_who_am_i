package com.eleks.academy.whoami.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface Player {

    String getId();

    CompletableFuture<String> getCurrentQuestion(long limit, TimeUnit unit);

}