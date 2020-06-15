package com.assignment.distributedfilesharingapp.common;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Slf4j
public class StringEncoderDecoder {

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred while converting the text", e);
            return str;
        }
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred while converting the text", e);
            return str;
        }
    }
}
