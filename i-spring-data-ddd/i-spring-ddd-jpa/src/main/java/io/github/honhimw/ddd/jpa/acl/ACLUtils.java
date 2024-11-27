package io.github.honhimw.ddd.jpa.acl;

import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2023-09-21
 */

@Slf4j
public class ACLUtils {

    public static final Pattern CONTEXT_ATTRIBUTE_PATTERN = Pattern.compile("^\\{\\{(?<attr>\\w+)}}$");
    public static final Pattern GROUPED_PARAMETER_NAME = Pattern.compile("^(?<group>\\w+):(?<paramName>.+)$");

    public static final Pattern SUB_QUERY_PATTERN = Pattern.compile("^__subQuery:\\[(?<rtProperty>[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*)](?<className>[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*)#(?<property>[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*):(?<value>.*)$");
    public static final Pattern SUB_QUERY_PATTERN_V2 = Pattern.compile("^__subQueryV2:\\[(?<rtProperty>[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*)](?<dataDomain>[a-zA-Z\\d\\-_:]+)#(?<property>[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*):(?<value>.*)$");

    public static final String USER_ALL = "user:*";
    @SuppressWarnings("unused")
    public static final String ACCOUNT_ALL = "account:*";
    public static final String ROLE_ALL = "role:*";

    public static final Map<String, EntityType<?>> DATA_DOMAIN_ENTITY_MAP = new ConcurrentHashMap<>();

    public static String userACL(String userId) {
        return "user:%s".formatted(userId);
    }

    @SuppressWarnings("unused")
    public static String accountACL(String accountId) {
        return "account:%s".formatted(accountId);
    }

    public static String roleACL(String roleId) {
        return "role:%s".formatted(roleId);
    }

    @SuppressWarnings("unused")
    public static String placeholder(String attribute) {
        return "{{%s}}".formatted(attribute);
    }

    @SuppressWarnings("unused")
    public static String subQueryACL(Class<?> doType, String rtProperty, String conditionProperty, String value) {
        return "__subQuery:[%s]%s#%s:%s".formatted(rtProperty, doType.getName(), conditionProperty, value);
    }

}
