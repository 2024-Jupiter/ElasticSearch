package com.myfeed.model.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDto {
    @NotBlank(message = "제목을 입력 하세요.")
    private String title;

    @NotBlank(message = "내용을 입력 하세요.")
    @Size(min = 1, max = 500, message = "한 글자 이상 내용을 입력 하세요.")
    private String Content;

    private List<ImageDto> images = new ArrayList<>();
}
