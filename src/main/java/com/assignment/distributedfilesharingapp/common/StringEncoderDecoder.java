package com.assignment.distributedfilesharingapp.common;

import com.sun.xml.internal.ws.commons.xmlutil.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Slf4j
public class StringEncoderDecoder {

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, Converter.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred while converting the text", e);
            return str;
        }
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, Converter.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred while converting the text", e);
            return str;
        }
    }
}
