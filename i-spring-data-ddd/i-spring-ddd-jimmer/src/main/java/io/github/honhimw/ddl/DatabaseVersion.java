package io.github.honhimw.ddl;

/**
 * @author honhimW
 * @since 2025-07-01
 */

public record DatabaseVersion(int major, int minor, String productVersion) {

    public boolean isSameOrAfter(int major) {
        return this.major >= major;
    }

    public boolean isSameOrAfter(int major, int minor) {
        return this.major > major || (this.major == major && this.minor >= minor);
    }

}
