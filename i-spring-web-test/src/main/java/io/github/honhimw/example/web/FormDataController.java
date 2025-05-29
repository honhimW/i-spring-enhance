package io.github.honhimw.example.web;

import io.github.honhimw.core.IResult;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hon_him
 * @since 2025-04-01
 */

@RestController
@RequestMapping("/form-data")
public class FormDataController {

    @PostMapping("/post")
    public IResult<Object> post(@RequestPart("text") String raw, @RequestPart("file") FilePart filePart) {
        String filename = filePart.filename();
        return IResult.ok("%s: %s".formatted(raw, filename));
    }

}
