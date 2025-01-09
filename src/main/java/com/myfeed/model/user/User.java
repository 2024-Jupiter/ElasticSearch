package com.myfeed.model.user;

import com.myfeed.model.base.BaseTimeEntity;
import com.myfeed.model.post.BlockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // 필수값

    @Column(nullable = false)
    private String password; // 필수값

    @Column(nullable = false)
    private String username;

    @Column
    private String phoneNumber; // 폼 로그인 시 필수값

    @Column(nullable = false)
    private String nickname; // 필수값

    @Column
    private String profileImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;

    //@Column(nullable = false, name = "status")
    @Column(nullable = false, name = "status", columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isActive = true; // status

    //@Column(nullable = false)
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;
}
