package com.ccsidd.rtone.objects;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by dung on 12/24/15.
 */
public class Setting extends RealmObject {

    @Required
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
