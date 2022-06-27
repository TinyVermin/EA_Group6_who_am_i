package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.model.request.PlayersAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnsweringPlayer {

    private String name;
    private PlayersAnswer answer;
}
