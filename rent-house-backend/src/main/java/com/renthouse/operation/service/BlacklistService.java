package com.renthouse.operation.service;

import com.renthouse.operation.BlacklistController.Request;

public interface BlacklistService {
    void block(long tenantId, Request r);
    void unblock(long tenantId);
}
