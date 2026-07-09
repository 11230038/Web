package com.example.end.service.impl;

import com.example.end.mapper.OperateLogMapper;
import com.example.end.pojo.OperateLog;
import com.example.end.pojo.OperateLogPage;
import com.example.end.service.OperateLogService;
import org.springframework.stereotype.Service;

@Service
public class OperateLogImpl implements OperateLogService {
    private final OperateLogMapper operateLogMapper;

    public OperateLogImpl(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    @Override
    public OperateLog add(OperateLog operateLog) {
        operateLogMapper.add(operateLog);
        return operateLog;
    }

    @Override
    public OperateLogPage getPage(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int total = operateLogMapper.count();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) safePageSize));
        int normalizedPage = Math.min(safePage, totalPages);
        int offset = (normalizedPage - 1) * safePageSize;

        OperateLogPage result = new OperateLogPage();
        result.setPage(normalizedPage);
        result.setPageSize(safePageSize);
        result.setTotal(total);
        result.setTotalPages(totalPages);
        result.setItems(operateLogMapper.selectPage(offset, safePageSize));
        return result;
    }
}
