package io.github.honhimw.ddd.jimmer.convert;

import jakarta.annotation.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-05-28
 */
public class PathNode {

    String name;
    @Nullable
    PathNode parent;
    List<PathNode> siblings = new ArrayList<>();
    @Nullable
    Object value;

    public PathNode(String edge, @Nullable PathNode parent, @Nullable Object value) {

        this.name = edge;
        this.parent = parent;
        this.value = value;
    }

    public PathNode add(String attribute, @Nullable Object value) {

        PathNode node = new PathNode(attribute, this, value);
        siblings.add(node);
        return node;
    }

    public boolean spansCycle() {

        if (value == null) {
            return false;
        }

        String identityHex = ObjectUtils.getIdentityHexString(value);
        PathNode current = parent;

        while (current != null) {

            if (current.value != null && ObjectUtils.getIdentityHexString(current.value).equals(identityHex)) {
                return true;
            }
            current = current.parent;
        }

        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent);
            sb.append(" -");
            sb.append(name);
            sb.append("-> ");
        }

        sb.append("[{ ");
        sb.append(ObjectUtils.nullSafeToString(value));
        sb.append(" }]");
        return sb.toString();
    }
}
