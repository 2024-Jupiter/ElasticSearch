package com.myfeed.model.post;

import com.myfeed.model.base.BaseTimeEntity;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", columnDefinition = "int default 0")
    private int viewCount = 0;

    @Column(name = "like_count", columnDefinition = "int default 0")
    private int likeCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BlockStatus status = BlockStatus.NORMAL_STATUS;

    public void addReply(Reply reply) {
        if (this.replies == null)
            this.replies = new ArrayList<>();
        this.replies.add(reply);
        reply.setPost(this);
    }

    public void addImage(Image image) {
        if (this.images == null)
            this.images = new ArrayList<>();
        this.images.add(image);
        image.setPost(this);
    }
}
