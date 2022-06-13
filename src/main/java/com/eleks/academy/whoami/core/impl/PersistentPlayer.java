package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.Objects;

public class PersistentPlayer implements SynchronousPlayer {

    private String name;
    private String character;

    public PersistentPlayer(String name) {
        this.name = Objects.requireNonNull(name);
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
}