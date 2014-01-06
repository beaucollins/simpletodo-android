package com.simperium.simpletodo;

import com.simperium.client.Bucket;
import com.simperium.client.BucketObject;
import com.simperium.client.BucketSchema;
import com.simperium.client.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class Todo extends BucketObject {

    public static class Schema extends BucketSchema<Todo> {

        public static final String BUCKET_NAME = "todo";

        public Schema() {
            // autoIndex indexes all top level properties: done, title and order
            autoIndex();
            // Todo instances by default have { "done": 0 }
            setDefault(DONE_PROPERTY, NOT_DONE);

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
        return "Todo " + getSimperiumKey() + ": " + getTitle() + " [" + (isDone() ? "✓" : " " ) + "]";
    }

    protected void updateProperties(JSONObject properties) {
        this.properties = properties;
    }

    public void toggleDone() {
        setProperty(DONE_PROPERTY, isDone() ? NOT_DONE : DONE);
        save();
    }

    public String getTitle() {
        return properties.optString(TITLE_PROPERTY, "");
    }

    public void setTitle(String title) {
        setProperty(TITLE_PROPERTY, title);
    }

    /**
     * Simpletodo on iOS uses boolean true/false JSON while the web app uses
     * an integer (1=true, 0=done) instead.
     * 
     * We use an int but we need to check for boolean as well.
     */
    public boolean isDone() {
        try {
            // is done property an int of 1?
            return properties.getInt(DONE_PROPERTY) == DONE;
        } catch (JSONException e) {
            // done wasn't an int
            try {
                // is done boolean true?
                return properties.getBoolean(DONE_PROPERTY);
            } catch (JSONException e1) {
                // this was unexpected, it should have been an int or a boolean but was neither, we'll just return not done in this case
                return false;
            }
        }
    }

    public void setOrder(int order) {
        setProperty(ORDER_PROPERTY, order);
    }

    public void save() {
        android.util.Log.d("Simpletodo", properties.toString());
        super.save();
    }

}