package com.myfeed.email.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailMessage {
    private String to;
    private String subject;
    private String message;
}