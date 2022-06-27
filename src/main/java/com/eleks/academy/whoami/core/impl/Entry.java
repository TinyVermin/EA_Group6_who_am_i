package com.eleks.academy.whoami.core.impl;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Entry {

    private Integer id;
    private String playerName;
    private String playerQuestion;
    private List<AnsweringPlayer> answers = new ArrayList<>();

    public Entry(String playerName, String playerQuestion) {
        this.playerName = playerName;
        this.playerQuestion = playerQuestion;
    }

    public void addPlayerWithAnswer(AnsweringPlayer player) {
        this.answers.add(player);
    }
}
