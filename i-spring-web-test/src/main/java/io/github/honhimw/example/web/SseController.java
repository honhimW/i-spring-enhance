package io.github.honhimw.example.web;

import io.github.honhimw.core.IResult;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author hon_him
 * @since 2025-03-26
 */

@RestController
@RequestMapping("/sse")
public class SseController {

    @GetMapping("/open/{name}")
    public Flux<ServerSentEvent<String>> open(@PathVariable("name") String name) {
        return Flux.interval(Duration.ofSeconds(2))
            .map(sequence -> ServerSentEvent.builder("Event #" + sequence).build())
            .doOnSubscribe(sub -> System.out.println("Client connected")).take(5);
    }

}
