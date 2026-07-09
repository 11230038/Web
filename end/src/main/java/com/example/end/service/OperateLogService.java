package com.example.end.service;

import com.example.end.pojo.OperateLog;
import com.example.end.pojo.OperateLogPage;

public interface OperateLogService {
    OperateLog add(OperateLog operateLog);

    OperateLogPage getPage(Integer page, Integer pageSize);
}
