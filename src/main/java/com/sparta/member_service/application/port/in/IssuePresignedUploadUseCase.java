package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadResultDto;

public interface IssuePresignedUploadUseCase {

	IssuePresignedUploadResultDto issuePresignedUpload(IssuePresignedUploadCommand command);
}
