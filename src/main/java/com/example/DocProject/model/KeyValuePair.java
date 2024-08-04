package com.example.DocProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

/*
 * This class hold to flatten json data .
 */
public class KeyValuePair {
    private String key;
    private String value;
    private String type; // Will be used only for arrays
    private int arraySize; // Will be used only for arrays

    @Override
    public String toString() {
        return  "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", arraySize=" + arraySize ;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KeyValuePair that = (KeyValuePair) obj;
        return key.equals(that.key) && value.equals(that.value);
    }
}
