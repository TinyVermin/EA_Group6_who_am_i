package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class PersistentPlayer implements SynchronousPlayer {
    @JsonIgnore
    private final String id;
    private String name;
    private String character;

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
}