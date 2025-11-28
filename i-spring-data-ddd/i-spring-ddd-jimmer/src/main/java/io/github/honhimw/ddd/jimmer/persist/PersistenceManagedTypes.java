package io.github.honhimw.ddd.jimmer.persist;

import java.util.Collections;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-09-05
 */

public class PersistenceManagedTypes {

    private final List<String> managedClassNames;

    private final List<String> managedPackages;

    public PersistenceManagedTypes(List<String> managedClassNames, List<String> managedPackages) {
        this.managedClassNames = Collections.unmodifiableList(managedClassNames);
        this.managedPackages = Collections.unmodifiableList(managedPackages);
    }

    public List<String> getManagedClassNames() {
        return this.managedClassNames;
    }

    public List<String> getManagedPackages() {
        return this.managedPackages;
    }
}
