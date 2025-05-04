package org.strangerlink.userservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private long createdAt; // cambiato da LocalDateTime a long
    private long lastActive; // cambiato da LocalDateTime a long
}