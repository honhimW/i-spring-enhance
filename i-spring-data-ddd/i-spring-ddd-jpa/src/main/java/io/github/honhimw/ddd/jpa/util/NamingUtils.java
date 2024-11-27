package io.github.honhimw.ddd.jpa.util;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2022-10-18
 */

public class NamingUtils {

    private final Charset charset;

    public NamingUtils(Charset charset) {
        this.charset = Objects.requireNonNullElse(charset, StandardCharsets.UTF_8);
    }

    public static NamingUtils of(String charset) {
        if (StringUtils.isNotBlank(charset)) {
            return new NamingUtils(Charset.forName(charset));
        } else {
            return new NamingUtils(StandardCharsets.UTF_8);
        }
    }

    public String genName(String prefix, Identifier table, List<Identifier> columns) {
        StringBuilder sb = new StringBuilder(table.toString());
        Identifier[] identifiers = columns.toArray(Identifier[]::new);
        Arrays.sort(
            identifiers,
            Comparator.comparing(Identifier::getCanonicalName)
        );
        for (Identifier columnName : identifiers) {
            sb.append("_").append(columnName);
        }
        return prefix + hashedName(sb.toString());
    }

    public String hashedName(String name) {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, charset);
    }

}
