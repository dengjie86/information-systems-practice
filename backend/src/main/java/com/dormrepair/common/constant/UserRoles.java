package com.dormrepair.common.constant;

import java.util.Set;

public final class UserRoles {

    public static final String STUDENT = "STUDENT";
    public static final String ADMIN = "ADMIN";
    public static final String WORKER = "WORKER";
    public static final Set<String> ALL = Set.of(STUDENT, ADMIN, WORKER);

    private UserRoles() {
    }
}
