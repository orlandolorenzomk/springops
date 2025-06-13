package org.kreyzon.springops.common.dto.email;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MailDto {

    private final String subject;
    private final String body;
    private final String receiver;
    private final List<String> cc;
    private final List<Attachment> attachments;
}
