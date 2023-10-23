package com.tj.wegocodingexercise.util;

import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ResourceProvider {

    public InputStream getInputStream(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + resourcePath);
        }

        return inputStream;
    }
}
