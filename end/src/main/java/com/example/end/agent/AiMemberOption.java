package com.example.end.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiMemberOption {
    private Integer id;

    private String name;

    private Integer role;

    private String roleName;
}
