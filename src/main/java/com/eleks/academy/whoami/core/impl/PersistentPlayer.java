package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.TimeoutException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

public class PersistentPlayer implements SynchronousPlayer {
    @JsonIgnore
    private final String id;
    private String name;
    private String character;
    private final Queue<String> answerQueue = new ConcurrentLinkedQueue<>();

    public PersistentPlayer(String name, String id) {
        this.name = Objects.requireNonNull(name);
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SynchronousPlayer setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void setCharacter(String character) {
        this.character = character;
    }

    @Override
    public String getCharacter() {
        return this.character;
    }

    @Override
    public void setAnswer(Answer answer) {
        switch (answer.getState()) {
            case ASKING -> setCurrentQuestion(answer.getMessage());
        }
    }

    @Override
    @JsonIgnore
    public CompletableFuture<String> getCurrentQuestion(long limit, TimeUnit unit) {
        return CompletableFuture
                .supplyAsync(() -> waitAnswer(limit, unit));
    }

    private String waitAnswer(long limit, TimeUnit unit) throws TimeoutException {
        var currentTime = System.currentTimeMillis();
        while (answerQueue.isEmpty()) {
            if (System.currentTimeMillis() - currentTime >= unit.toMillis(limit)) {
                throw new TimeoutException();
            }
        }
        return answerQueue.poll();
    }

    private void setCurrentQuestion(String currentQuestion) {
        answerQueue.add(currentQuestion);
    }

}