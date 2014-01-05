package com.simperium.simpletodo;

import com.simperium.client.Bucket;
import com.simperium.client.BucketObject;
import com.simperium.client.BucketSchema;
import com.simperium.client.Query;

import org.json.JSONObject;

public class Todo extends BucketObject {

    public static class Schema extends BucketSchema<Todo> {

        public static final String BUCKET_NAME = "todo";

        public Schema() {
            // autoIndex indexes all top level properties: done, title and order
            autoIndex();
        }

        public String getRemoteName() {
            return BUCKET_NAME;
        }

        public Todo build(String key, JSONObject properties) {
            return new Todo(key, properties);
        }

        public void update(Todo todo, JSONObject properties) {
            todo.updateProperties(properties);
            android.util.Log.d("Simpletodo", "Updated properties: " + todo);
        }

    }


    public static final String DONE_PROPERTY = "done";
    public static final String TITLE_PROPERTY = "title";
    public static final String ORDER_PROPERTY = "order";

    public static final int DONE = 1;
    public static final int NOT_DONE = 0;

    public Todo(String key, JSONObject properties) {
        super(key, properties);
    }

    public static Query<Todo> queryAll(Bucket<Todo> bucket) {
        Query<Todo> query = bucket.query();
        query.order(ORDER_PROPERTY);
        return query;
    }

    public String toString() {
        return "Todo: " + getTitle() + " checked: " + (isChecked() ? "yes" : "no" ) + "\n" + properties.toString(); 
    }

    protected void updateProperties(JSONObject properties) {
        this.properties = properties;
    }

    public void toggleDone() {
        setProperty(DONE_PROPERTY, isChecked() ? NOT_DONE : DONE);
        save();
    }

    public String getTitle() {
        return properties.optString(TITLE_PROPERTY, "");
    }

    public void setTitle(String title) {
        setProperty(TITLE_PROPERTY, title);
    }

    public boolean isChecked() {
        return properties.optInt(DONE_PROPERTY, NOT_DONE) == DONE;
    }

    public void setOrder(int order) {
        setProperty(ORDER_PROPERTY, order);
    }

    public void save() {
        android.util.Log.d("Simpletodo", properties.toString());
        super.save();
    }

}