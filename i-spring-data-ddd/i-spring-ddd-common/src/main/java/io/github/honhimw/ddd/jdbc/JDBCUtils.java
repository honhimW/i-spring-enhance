package io.github.honhimw.ddd.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.util.JsonUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-03-12
 */

public class JDBCUtils {

    public static List<Map<String, Object>> toMap(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount + 1];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i] = metaData.getColumnLabel(i);
        }

        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = resultSet.getObject(i);
                rowMap.put(columnNames[i], columnValue);
            }

            resultList.add(rowMap);
        }

        return resultList;
    }

    public static ArrayNode toNode(ResultSet resultSet) throws SQLException {
        ObjectMapper mapper = JsonUtils.mapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount + 1];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i] = metaData.getColumnLabel(i);
        }

        while (resultSet.next()) {
            ObjectNode objectNode = mapper.createObjectNode();
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = resultSet.getObject(i);
                objectNode.putPOJO(columnNames[i], columnValue);
            }
            arrayNode.add(objectNode);
        }

        return arrayNode;
    }

}
