package com.careround.shared.security;

import com.careround.auth.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class HospitalContextHolder {

    public record HospitalContext(String hospitalId, String userId, UserRole role) {}

    private static final ThreadLocal<HospitalContext> CONTEXT = new ThreadLocal<>();

    public static void set(String hospitalId, String userId, UserRole role) {
        CONTEXT.set(new HospitalContext(hospitalId, userId, role));
    }

    private static HospitalContext requireContext() {
        HospitalContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException(
                    "No hospital context found for current thread. Request must be authenticated.");
        }
        return ctx;
    }

    public static String getHospitalId() {
        return requireContext().hospitalId();
    }

    public static String getUserId() {
        return requireContext().userId();
    }

    public static UserRole getRole() {
        return requireContext().role();
    }

    public static boolean hasRole(UserRole userRole) {
        return requireContext().role().equals(userRole);
    }

    public static boolean hasAnyRole(UserRole... roles) {
        UserRole current = requireContext().role();
        for (UserRole r : roles) {
            if (current == r) return true;
        }
        return false;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
