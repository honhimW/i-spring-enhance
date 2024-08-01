package io.github.honhimw.spring.cache.redis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.core.RedisCommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2023-06-27
 */

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class RedisMessageEvent extends ApplicationEvent {

    private static final String DATABASE = "database";
    private static final String COMMAND = "command";

    public static final Pattern KEY_EVENT_PATTERN = Pattern.compile("^__keyevent@(?<" + DATABASE + ">\\d+)__:(?<" + COMMAND + ">.+)$");

    public RedisMessageEvent() {
        super(new Object());
    }

    public RedisMessageEvent(String channel, String body) {
        this();
        this.channel = channel;
        this.body = body;
    }

    private String channel;

    private String body;

    private Integer database;

    private RedisCommand command;

    public boolean fromKeyEvent() {
        if (StringUtils.isNotBlank(this.channel)) {
            Matcher matcher = KEY_EVENT_PATTERN.matcher(this.channel);
            if (matcher.find()) {
                String database = matcher.group(DATABASE);
                this.database = Integer.parseInt(database);
                String command = matcher.group(COMMAND);
                this.command = RedisCommand.failsafeCommandLookup(command);
                return true;
            }
        }
        return false;
    }

}
