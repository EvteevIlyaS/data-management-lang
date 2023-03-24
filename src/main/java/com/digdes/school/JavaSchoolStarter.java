package com.digdes.school;

import java.util.*;

public class JavaSchoolStarter {
    private final DataManager dataManager;
    private boolean isConditional;
    private String[] splitRequest;

    public JavaSchoolStarter() {
        dataManager = new DataManager();
    }

    public List<Map<String, Object>> execute(String request) {
        List<Map<String, Object>> resData;

        splitRequest = request.trim().split("\\s+");
        String requestType = splitRequest[0].toLowerCase();
        splitRequest = Arrays.copyOfRange(splitRequest, 1, splitRequest.length);

        isConditional = request.toLowerCase().contains(" where ");
        try {
            switch (requestType) {
                case "insert": {
                    resData = onInsert();
                    break;
                }
                case "update": {
                    resData = onUpdate();
                    break;
                }
                case "delete": {
                    resData = onDelete();
                    break;
                }
                case "select": {
                    resData = onSelect();
                    break;
                }
                default:
                    throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("Wrong request!");
            resData = new ArrayList<>();
        }

        return resData;
    }

    private List<Map<String, Object>> onInsert() {
        splitRequest = Arrays.copyOfRange(splitRequest, 1, splitRequest.length);
        return dataManager.insert(String.join(" ", splitRequest));
    }

    private List<Map<String, Object>> onSelect() throws Exception {
        if (splitRequest.length != 0) {
            return dataManager.select(String.join(" ", Arrays.copyOfRange(splitRequest, 1, splitRequest.length))
            );
        }
        return dataManager.selectAll();
    }

    private List<Map<String, Object>> onDelete() throws Exception {
        if (splitRequest.length != 0) {
            return dataManager.delete(String.join(" ", Arrays.copyOfRange(splitRequest, 1, splitRequest.length)));
        } else {
            return dataManager.deleteAll();
        }
    }

    private List<Map<String, Object>> onUpdate() throws Exception {
        splitRequest = Arrays.copyOfRange(splitRequest, 1, splitRequest.length);
        String values = String.join(" ", splitRequest);

        if (isConditional) {
            String[] valuesConditions = values.split("(?i) where ");
            String conditions = valuesConditions[1];
            values = valuesConditions[0];

            return dataManager.update(values, conditions);
        } else {
            return dataManager.updateAll(values);
        }

    }


}
