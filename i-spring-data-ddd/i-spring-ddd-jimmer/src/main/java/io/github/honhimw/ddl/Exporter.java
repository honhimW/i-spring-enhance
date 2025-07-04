package io.github.honhimw.ddl;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-06-17
 */

public interface Exporter<T> {

    List<String> getSqlCreateStrings(T exportable);

    List<String> getSqlDropStrings(T exportable);

}
