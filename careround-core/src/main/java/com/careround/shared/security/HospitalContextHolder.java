package com.careround.shared.security;

import com.careround.auth.enums.UserRole;

public final class HospitalContextHolder {

    private static final ThreadLocal<String> hospitalIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<UserRole> roleHolder = new ThreadLocal<>();

    private HospitalContextHolder() {}

    public static void setHospitalId(String hospitalId) {
        hospitalIdHolder.set(hospitalId);
    }

    public static String getHospitalId() {
        return hospitalIdHolder.get();
    }

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static void setRole(UserRole role) {
        roleHolder.set(role);
    }

    public static UserRole getRole() {
        return roleHolder.get();
    }

    public static void clear() {
        hospitalIdHolder.remove();
        userIdHolder.remove();
        roleHolder.remove();
    }
}
