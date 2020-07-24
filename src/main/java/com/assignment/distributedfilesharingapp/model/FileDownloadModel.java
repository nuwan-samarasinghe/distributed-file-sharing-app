package com.assignment.distributedfilesharingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class FileDownloadModel {
    private String message;
    private HttpStatus httpStatus;
}
