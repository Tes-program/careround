package com.careround.notification;

import com.careround.notification.dto.NotificationResponse;
import com.careround.notification.dto.UnreadCountResponse;
import com.careround.notification.service.NotificationService;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Authenticated user's clinical and system notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications", description = "Returns role-scoped clinical and system notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.listNotifications()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", description = "Returns the number of unread notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.unreadCount()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks one notification as read for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable String id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all currently visible notifications as read for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok(ApiResponse.ok("Notifications marked as read", null));
    }
}
