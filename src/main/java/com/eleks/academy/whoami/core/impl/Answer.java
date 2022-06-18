package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.model.response.PlayerState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Answer {

    private PlayerState state;

    private String message;

}
