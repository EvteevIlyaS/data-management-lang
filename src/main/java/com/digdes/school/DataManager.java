package com.digdes.school;

import java.util.*;

import static com.digdes.school.DataParser.executeConditions;
import static com.digdes.school.DataParser.splitValues;
import static com.digdes.school.Tools.nullFilter;

public class DataManager {

    private List<Map<String, Object>> dataCollection;

    public DataManager() {
        dataCollection = new ArrayList<>();
    }

    public List<Map<String, Object>> insert(String values) {
        List<Map<String, Object>> resData = new ArrayList<>();

        Map<String, Object> resRow = new HashMap<>();
        resRow.put("id", null);
        resRow.put("lastname", null);
        resRow.put("age", null);
        resRow.put("cost", null);
        resRow.put("active", null);

        Map<String, Object> row = splitValues(values);
        resRow.putAll(row);

        if (resRow.values().stream().allMatch(Objects::isNull)) throw new RuntimeException();

        dataCollection.add(resRow);
        resData.add(resRow);

        return nullFilter(resData);
    }

    public List<Map<String, Object>> select(String conditions) throws Exception {
        List<Map<String, Object>> resData = new ArrayList<>();

        Set<Integer> selectIndices = executeConditions(conditions, dataCollection);

        selectIndices.forEach(idx -> resData.add(dataCollection.get(idx)));

        return nullFilter(resData);
    }

    public List<Map<String, Object>> selectAll() {
        List<Map<String, Object>> resData = new ArrayList<>(dataCollection);

        return nullFilter(resData);
    }

    public List<Map<String, Object>> delete(String conditions) throws Exception {
        List<Map<String, Object>> resData = new ArrayList<>();

        List<Map<String, Object>> tmpDataCollection = new ArrayList<>();
        Set<Integer> deleteIndices = executeConditions(conditions, dataCollection);

        deleteIndices.forEach(idx -> resData.add(dataCollection.get(idx)));

        for (int i = 0; i < dataCollection.size(); i++) {
            if (!deleteIndices.contains(i)) tmpDataCollection.add(dataCollection.get(i));
        }
        dataCollection = tmpDataCollection;

        return nullFilter(resData);
    }

    public List<Map<String, Object>> deleteAll() {
        List<Map<String, Object>> resData = new ArrayList<>(dataCollection);
        dataCollection.clear();

        return nullFilter(resData);
    }

    public List<Map<String, Object>> update(String values, String conditions) throws Exception {
        List<Map<String, Object>> resData = new ArrayList<>();

        Map<String, Object> updateData = splitValues(values);
        Set<Integer> updateIndices = executeConditions(conditions, dataCollection);
        updateIndices.forEach(idx -> dataCollection.get(idx).putAll(updateData));

        updateIndices.forEach(idx -> resData.add(dataCollection.get(idx)));

        return nullFilter(resData);
    }

    public List<Map<String, Object>> updateAll(String values) {
        Map<String, Object> updateData = splitValues(values);
        dataCollection.forEach(row -> row.putAll(updateData));

        return nullFilter(new ArrayList<>(dataCollection));
    }
}
