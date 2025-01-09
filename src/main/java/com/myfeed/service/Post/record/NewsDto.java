package com.myfeed.service.Post.record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NewsDto(String title, String content, String press, String author,String date,String id) {

    public LocalDateTime getParsedDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(date, formatter).atStartOfDay();
    };
}
