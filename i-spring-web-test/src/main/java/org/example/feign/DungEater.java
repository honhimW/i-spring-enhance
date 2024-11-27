package org.example.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hon_him
 * @since 2024-08-09
 */

@FeignClient(
    name = "dung-eater"
)
public interface DungEater {

    @GetMapping("/eat")
    String eat(@RequestParam("shit") String shit);

}
