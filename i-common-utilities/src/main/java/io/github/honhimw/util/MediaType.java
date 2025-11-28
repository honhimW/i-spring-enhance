package io.github.honhimw.util;

import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copied from spring MimeType
 *
 * @author honhimW
 * @since 2025-09-28
 */

public final class MediaType {

    public static final String WILDCARD_TYPE = "*";

    private static final Map<String, MediaType> CACHE = new ConcurrentHashMap<>();

    public static MediaType get(@NonNull String mimeType) {
        return CACHE.compute(mimeType, (s, mediaType) -> {
            if (mediaType == null) {
                mediaType = parse(s);
            }
            return mediaType;
        });
    }

    public static MediaType parse(@NonNull String mimeType) {
        int index = mimeType.indexOf(';');
        String fullType = (index >= 0 ? mimeType.substring(0, index) : mimeType).trim();
        if (fullType.isEmpty()) {
            throw new IllegalArgumentException("'mimeType' must not be empty");
        }

        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new IllegalArgumentException("'mimeType' does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new IllegalArgumentException("'mimeType' does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1);
        if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
            throw new IllegalArgumentException("'mimeType' wildcard type is legal only in '*/*' (all mime types)");
        }

        Map<String, String> parameters = new LinkedHashMap<>(4);
        do {
            int nextIndex = index + 1;
            boolean quoted = false;
            while (nextIndex < mimeType.length()) {
                char ch = mimeType.charAt(nextIndex);
                if (ch == ';') {
                    if (!quoted) {
                        break;
                    }
                } else if (ch == '"') {
                    quoted = !quoted;
                }
                nextIndex++;
            }
            String parameter = mimeType.substring(index + 1, nextIndex).trim();
            if (!parameter.isEmpty()) {
                int eqIndex = parameter.indexOf('=');
                if (eqIndex >= 0) {
                    String attribute = parameter.substring(0, eqIndex).trim();
                    String value = parameter.substring(eqIndex + 1).trim();
                    parameters.put(attribute, value);
                }
            }
            index = nextIndex;
        }
        while (index < mimeType.length());

        return new MediaType(type, subtype, parameters);
    }

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;
    private final Charset charset;

    private MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = Collections.unmodifiableMap(parameters);
        String charset = parameters.get("charset");
        if (charset != null) {
            this.charset = Charset.forName(unquote(charset));
        } else {
            this.charset = StandardCharsets.UTF_8;
        }
    }

    public String type() {
        return type;
    }

    public String subtype() {
        return subtype;
    }

    public Map<String, String> parameters() {
        return parameters;
    }

    public Charset charset() {
        return charset;
    }

    public boolean isCompatibleWith(@Nullable MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType() || other.isWildcardType()) {
            return true;
        } else if (type().equals(other.type())) {
            if (subtype().equals(other.subtype())) {
                return true;
            }
            if (isWildcardSubtype() || other.isWildcardSubtype()) {
                String thisSuffix = getSubtypeSuffix();
                String otherSuffix = other.getSubtypeSuffix();
                if (subtype().equals(WILDCARD_TYPE) || other.subtype().equals(WILDCARD_TYPE)) {
                    return true;
                } else if (isWildcardSubtype() && thisSuffix != null) {
                    return (thisSuffix.equals(other.subtype()) || thisSuffix.equals(otherSuffix));
                } else if (other.isWildcardSubtype() && otherSuffix != null) {
                    return (subtype().equals(otherSuffix) || otherSuffix.equals(thisSuffix));
                }
            }
        }
        return false;
    }

    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(type());
    }

    public boolean isWildcardSubtype() {
        String subtype = subtype();
        return (WILDCARD_TYPE.equals(subtype) || subtype.startsWith("*+"));
    }

    @Nullable
    public String getSubtypeSuffix() {
        int suffixIndex = this.subtype.lastIndexOf('+');
        if (suffixIndex != -1) {
            return this.subtype.substring(suffixIndex + 1);
        }
        return null;
    }

    public boolean isRawType() {
        if ("text".equals(type)) {
            return true;
        }
        if ("application".equals(type)) {
            return subtype.contains("json")
                   || subtype.contains("x-www-form-urlencoded")
                   || subtype.contains("xml")
                   || subtype.contains("html")
                   || subtype.contains("csv")
                ;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append('/').append(subtype);
        this.parameters.forEach((key, val) -> {
            sb.append(';');
            sb.append(key);
            sb.append('=');
            sb.append(val);
        });
        return sb.toString();
    }

    private String unquote(String s) {
        return (isQuotedString(s) ? s.substring(1, s.length() - 1) : s);
    }

    private boolean isQuotedString(String s) {
        if (s.length() < 2) {
            return false;
        }
        return ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")));
    }

}
