package io.github.honhimw.ddl;

import lombok.Getter;

/**
 * @author honhimW
 * @since 2025-07-01
 */

@Getter
public class DatabaseVersion {

    private final int major;

    private final int minor;

    private final String productVersion;

    public DatabaseVersion(int major, int minor, String productVersion) {
        this.major = major;
        this.minor = minor;
        this.productVersion = productVersion;
    }

    public boolean isSameOrAfter(int major) {
        return this.major >= major;
    }

    public boolean isSameOrAfter(int major, int minor) {
        return this.major > major || (this.major == major && this.minor >= minor);
    }

}
