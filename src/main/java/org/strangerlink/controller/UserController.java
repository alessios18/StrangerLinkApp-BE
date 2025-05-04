package org.strangerlink.controller;

import org.strangerlink.dto.ProfileDto.ProfileSearchRequest;
import org.strangerlink.model.User;
import org.strangerlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestBody ProfileSearchRequest searchRequest) {
        return ResponseEntity.ok(userService.searchUsers(searchRequest));
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock/{userId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<User>> getBlockedUsers() {
        return ResponseEntity.ok(userService.getBlockedUsers());
    }
}