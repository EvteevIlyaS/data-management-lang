package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.digdes.school.Tools.matrixTranspose;
import static com.digdes.school.Tools.typeCaster;


public class DataParser {
    private static final Set<String> POSSIBLE_TITLES = new HashSet<>(
            Arrays.asList("id", "lastname", "cost", "age", "active"));

    private DataParser() {
    }

    private static List<String> getOperandsOperators(String conditions, ConditionalValues conditionalValues) throws Exception {
        String searchPattern;
        switch (conditionalValues) {
            case LOGICAL_OPERATORS: {
                searchPattern = "(?i)\\sor\\s|\\sand\\s";
                break;
            }
            case COMPARISON_OPERATORS: {
                searchPattern = "(?i)!?=|\\si?like\\s|[><]=?";
                break;
            }
            case OPERANDS: {
                searchPattern = "'%?[a-zA-Zа-яА-Я\\s]+%?'|(-?\\d+(?:\\.\\d+)?)|(?i)true|false";
                // Убрать из replaceAll \\s если между or, and, like, ilike могут отсутствовать пробелы
                conditions = conditions.replaceAll("(?i)\\sor\\s|\\sand\\s|\\si?like\\s", " ");
                break;
            }
            default:
                throw new Exception();
        }

        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(searchPattern).matcher(conditions);
        while (m.find()) allMatches.add(m.group().trim());
        return allMatches;
    }

    private static Map<String, List<String>> prepareParams(String conditions) throws Exception {
        Map<String, List<String>> res = new HashMap<>();

        List<String> operands = getOperandsOperators(conditions,
                ConditionalValues.OPERANDS);
        List<String> col = new ArrayList<>();
        List<String> val = new ArrayList<>();
        for (int i = 0; i < operands.size(); i++) {
            if (i % 2 == 0) {
                String currentCol = operands.get(i).toLowerCase().replace("'", "");
                if (POSSIBLE_TITLES.contains(currentCol)) col.add(currentCol);
                else throw new Exception();
            } else val.add(operands.get(i));
        }

        List<String> logicalOperators = getOperandsOperators(conditions,
                ConditionalValues.LOGICAL_OPERATORS).stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> comparisonOperators = getOperandsOperators(conditions,
                ConditionalValues.COMPARISON_OPERATORS).stream().map(String::toLowerCase).collect(Collectors.toList());

        res.put("columns", col);
        res.put("values", val);
        res.put("logical", logicalOperators);
        res.put("comparison", comparisonOperators);

        return res;
    }

    private static boolean executeLike(String str1, String str2, boolean caseSensitive) {
        if (!caseSensitive) {
            str1 = str1.toLowerCase();
            str2 = str2.toLowerCase();
        }

        String case1 = ".*" + str2.substring(1, str2.length() - 1) + ".*";
        String case2 = str2.substring(str2.length() - 1) + ".*";
        String case3 = ".*" + str2.substring(1);

        if (str2.startsWith("%") & str2.endsWith("%") & str1.matches(case1)) return true;
        else if (str2.endsWith("%") & str1.matches(case2)) return true;
        else if (str2.startsWith("%") & str1.matches(case3)) return true;
        else return !str2.endsWith("%") & !str2.endsWith("%") & str1.matches(str2);
    }

    private static Set<Integer> getTrueIndices(List<List<Boolean>> conditionsResults, List<String> logicalOperators) {
        Set<Integer> resIndices = new HashSet<>();

        List<Boolean> rowsConditionsResults = matrixTranspose(conditionsResults).stream().map(row -> {
            Queue<String> lo = new LinkedList<>(logicalOperators);
            return row.stream().reduce((cond1, cond2) -> {
                String nextLogicalOperators = lo.poll();
                return nextLogicalOperators.equals("or") ? cond1 | cond2 : cond1 & cond2;
            }).get();
        }).collect(Collectors.toList());

        for (int i = 0; i < rowsConditionsResults.size(); i++) {
            if (rowsConditionsResults.get(i)) resIndices.add(i);
        }

        return resIndices;
    }

    public static Map<String, Object> splitValues(String values) {
        Map<String, Object> fieldValue = new HashMap<>();

        List<String> valuesList = Arrays.asList(values.split("\\s*,\\s*"));
        valuesList.forEach(val -> {
            String[] tmpArr = val.split("\\s*=\\s*");
            String column = tmpArr[0].toLowerCase().replace("'", "");
            String value = tmpArr[1];
            if (value.equals("null")) fieldValue.put(column, null);
            else {
                try {
                    fieldValue.put(column, typeCaster(column, value));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return fieldValue;
    }

    public static Set<Integer> executeConditions(String conditions, List<Map<String, Object>> data) throws Exception {
        Map<String, List<String>> paramsMap = prepareParams(conditions);

        List<String> columnOperands = paramsMap.get("columns");
        List<String> valueOperands = paramsMap.get("values");
        List<String> comparisonOperators = paramsMap.get("comparison");
        List<String> logicalOperators = paramsMap.get("logical");


        List<List<Boolean>> conditionsResults = new ArrayList<>();
        for (int i = 0; i < comparisonOperators.size(); i++) {
            List<Boolean> curCondList = new ArrayList<>();
            conditionsResults.add(curCondList);

            String curCol = columnOperands.get(i);
            String curVal = valueOperands.get(i);

            switch (comparisonOperators.get(i)) {
                case "=": {
                    data.forEach(el -> {
                        try {
                            curCondList.add(typeCaster(curCol, curVal).equals(el.get(curCol)));
                        } catch (Exception e) {
                            curCondList.add(false);
                        }
                    });
                    break;
                }
                case "!=": {
                    data.forEach(el -> {
                        try {
                            curCondList.add(!typeCaster(curCol, curVal).equals(el.get(curCol)));
                        } catch (Exception e) {
                            curCondList.add(true);
                        }
                    });
                    break;
                }
                case "like": {
                    if (curCol.equals("lastname")) {
                        data.forEach(el -> {
                            if (el.get(curCol) == null) curCondList.add(false);
                            else {
                                try {
                                    curCondList.add(executeLike((String) el.get(curCol), (String) typeCaster(curCol, curVal), true));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } else throw new RuntimeException();
                    break;
                }
                case "ilike": {
                    if (curCol.equals("lastname")) {
                        data.forEach(el -> {
                            if (el.get(curCol) == null) curCondList.add(false);
                            else {
                                try {
                                    curCondList.add(executeLike((String) el.get(curCol), (String) typeCaster(curCol, curVal), false));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } else throw new RuntimeException();
                    break;
                }
                case ">=": {
                    data.forEach(el -> {
                        if (el.get(curCol) == null) curCondList.add(false);
                        else {
                            double checkValDouble = el.get(curCol) instanceof Long ? (double) (Long) el.get(curCol) : (double) el.get(curCol);
                            try {
                                curCondList.add(checkValDouble > Double.parseDouble(curVal) | typeCaster(curCol, curVal).equals(el.get(curCol)));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    break;
                }
                case "<=": {
                    data.forEach(el -> {
                        if (el.get(curCol) == null) curCondList.add(false);
                        else {
                            double checkValDouble = el.get(curCol) instanceof Long ? (double) (Long) el.get(curCol) : (double) el.get(curCol);
                            try {
                                curCondList.add(checkValDouble < Double.parseDouble(curVal) | typeCaster(curCol, curVal).equals(el.get(curCol)));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    break;
                }
                case "<": {
                    data.forEach(el -> {
                        if (el.get(curCol) == null) curCondList.add(false);
                        else {
                            double checkValDouble = el.get(curCol) instanceof Long ? (double) (Long) el.get(curCol) : (double) el.get(curCol);
                            curCondList.add(checkValDouble < Double.parseDouble(curVal));
                        }
                    });
                    break;
                }
                case ">": {
                    data.forEach(el -> {
                        if (el.get(curCol) == null) curCondList.add(false);
                        else {
                            double checkValDouble = el.get(curCol) instanceof Long ? (double) (Long) el.get(curCol) : (double) el.get(curCol);
                            curCondList.add(checkValDouble > Double.parseDouble(curVal));
                        }
                    });
                    break;
                }
            }
        }

        return getTrueIndices(conditionsResults, logicalOperators);

    }
}
