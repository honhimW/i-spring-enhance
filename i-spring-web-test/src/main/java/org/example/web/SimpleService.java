package org.example.web;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hon_him
 * @since 2024-08-09
 */

@Service
public class SimpleService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public void put(String id, String value) {
        cache.put(id, value);
    }

    @Cacheable("cacheable:test")
    public String get(String id) {
        return cache.get(id);
    }

}
