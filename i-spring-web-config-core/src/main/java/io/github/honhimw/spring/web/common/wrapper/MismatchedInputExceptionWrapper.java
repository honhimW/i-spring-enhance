package io.github.honhimw.spring.web.common.wrapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.github.honhimw.spring.web.common.SingleExceptionWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class MismatchedInputExceptionWrapper extends SingleExceptionWrapper<MismatchedInputException> {

    @NonNull
    @Override
    protected String _wrap(@NonNull MismatchedInputException e) {
        List<JsonMappingException.Reference> path = e.getPath();
        String fullPath = path.stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("/", "/", ""));
        return """
            {json.mismatched.input}
            %s
            """.formatted(fullPath);
    }

    @Override
    protected int _httpCode(@NonNull MismatchedInputException e) {
        return HttpStatus.BAD_REQUEST.value();
    }

}
