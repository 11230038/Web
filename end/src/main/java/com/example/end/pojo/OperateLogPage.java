package com.example.end.pojo;

import lombok.Data;

import java.util.List;

@Data
public class OperateLogPage {
    private Integer page;

    private Integer pageSize;

    private Integer total;

    private Integer totalPages;

    private List<OperateLog> items;
}
