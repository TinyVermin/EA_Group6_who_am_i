package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.Objects;

public class PersistentPlayer implements  SynchronousPlayer {

    private  String id;
    private final String name;

    public PersistentPlayer(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

}