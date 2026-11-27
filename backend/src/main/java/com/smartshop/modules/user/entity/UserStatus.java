package com.smartshop.modules.user.entity;

/**
 * Account lifecycle status matching {@code users.status} ENUM in DDL v2.1.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED,
    PENDING
}
