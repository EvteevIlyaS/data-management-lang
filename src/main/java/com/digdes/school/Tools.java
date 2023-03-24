package com.digdes.school;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Tools {
    private Tools() {
    }

    public static <T> List<List<T>> matrixTranspose(List<List<T>> list) {
        final int N = list.stream().mapToInt(List::size).max().orElse(-1);
        List<Iterator<T>> iterList = list.stream().map(List::iterator).collect(Collectors.toList());
        return IntStream.range(0, N)
                .mapToObj(n -> iterList.stream()
                        .filter(Iterator::hasNext)
                        .map(Iterator::next)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static List<Map<String, Object>> nullFilter(List<Map<String, Object>> resData) {
        return resData.stream().map(row -> row.entrySet().stream()
                .filter(el -> el.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ).collect(Collectors.toList());
    }

    public static void printTable(List<Map<String, Object>> maps) {
        List<String> keys = new ArrayList<>(Arrays.asList("id", "lastname", "age", "cost", "active"));

        System.out.print("| ");
        for (String key : keys) {
            System.out.printf("%-15s| ", key.toUpperCase());
        }
        System.out.println();

        for (Map<String, Object> map : maps) {
            System.out.print("| ");
            for (String key : keys) {
                String value = map.get(key) != null ? map.get(key).toString() : "";
                System.out.printf("%-15s| ", value);
            }
            System.out.println();
        }
        System.out.print("\n\n");
    }

    public static Object typeCaster(String column, String value) throws Exception {
        switch (column) {
            case "id":
            case "age": {
                return Long.parseLong(value);
            }
            case "lastname": {
                if (!value.startsWith("'") | !value.endsWith("'")) {
                    throw new Exception();
                }
                return value.replace("'", "");
            }
            case "cost": {
                return Double.parseDouble(value);
            }
            case "active": {
                return Boolean.parseBoolean(value);
            }
            default:
                throw new Exception();
        }
    }
}
