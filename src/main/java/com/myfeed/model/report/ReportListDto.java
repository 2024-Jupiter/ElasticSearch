package com.myfeed.model.report;

import com.myfeed.model.post.ImageDto;
import com.myfeed.model.post.Post;
import com.myfeed.model.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportListDto {
    private Long id;
    private String type;
    private LocalDateTime createdAt;
    private String status;

    public ReportListDto(Report report) {
        this.id = report.getId();
        this.type = String.valueOf(report.getType());
        this.createdAt = report.getCreatedAt();
        this.status = String.valueOf(report.getStatus());
    }
}
