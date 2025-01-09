package com.myfeed.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSyncEvent {
    private final Long postId;
    private final String operation; // CREATE, UPDATE, DELETE, VIEW_COUNT, LIKE_COUNT_UP, LIKE_COUNT_DOWN
}
