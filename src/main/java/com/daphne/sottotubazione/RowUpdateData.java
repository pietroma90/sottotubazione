package com.geowebframework.sottotubazione;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RowUpdateData {

    private Map<String, Object> updateValues = new ConcurrentHashMap<>();
    private Map<String, Object> filterValues = new ConcurrentHashMap<>();

    public void addValue(String key, Object value) {
        this.updateValues.put(key, value);
    }

    public void addFilter(String key, Object value) {
        this.filterValues.put(key, value);
    }
}