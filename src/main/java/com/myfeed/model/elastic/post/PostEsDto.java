package com.myfeed.model.elastic.post;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEsDto {
    @Column(name = "postEs")
    private PostEs postEs;

    @Column(name = "match_score")
    private double matchScore;
}
