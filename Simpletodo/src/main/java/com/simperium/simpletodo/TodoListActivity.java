package com.simperium.simpletodo;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;

import com.simperium.client.Bucket;
import com.simperium.android.SimperiumService;

public class TodoListActivity extends SimperiumService.ListActivity
implements Bucket.Listener<Todo>, OnItemClickListener, OnEditorActionListener {

    public final int ADD_ACTION_ID = 100;

    protected TodoAdapter mAdapter;
    protected Bucket<Todo> mTodos;
    protected EditText mEditText;

    @Override
    public Class getServiceClass() {
        return TodoService.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.todo_list);

        mAdapter = new TodoAdapter();
        ListView listView = getListView();
        // listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        mEditText = (EditText) findViewById(R.id.new_task_text);
        mEditText.setEnabled(false);
        mEditText.setOnEditorActionListener(this);

    }

    @Override
    public void onSimperiumConnected(SimperiumService service) {
        TodoService todoService = (TodoService) service;
        mTodos = todoService.getTodoBucket();
        mTodos.start();
        mTodos.addListener(this);
        refreshTodos(mTodos);
        mEditText.setEnabled(true);
    }

    @Override
    public void onSimperiumDisconnected() {
        mTodos.removeListener(this);
    }

    @Override
    public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
        if (actionId != ADD_ACTION_ID)
            return false;

        // clear the text view
        String label = tv.getText().toString();
        tv.getEditableText().clear();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0x0);

        Todo todo = mTodos.newObject();
        todo.setTitle(label);
        todo.setOrder(getListView().getCount());
        todo.save();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
        Todo todo = mAdapter.getItem(position);
        todo.toggleDone();
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkbox);
        checkbox.setChecked(todo.isChecked());
    }

    @Override
    public void onSaveObject(Bucket<Todo> todos, Todo todo) {
        refreshTodos(todos);
    }

    @Override
    public void onDeleteObject(Bucket<Todo> todos, Todo todo) {
        refreshTodos(todos);
    }

    @Override
    public void onChange(Bucket<Todo> todos, Bucket.ChangeType changeType, String key) {
        refreshTodos(todos);
    }

    @Override
    public void onBeforeUpdateObject(Bucket<Todo> todos, Todo todo) {
        // no-op
    }

    public void refreshTodos(final Bucket<Todo> todos) {

        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                mAdapter.requeryBucket(todos);
            }
        });

    }

    class TodoAdapter extends CursorAdapter {

        TodoAdapter() {
            super(TodoListActivity.this, null, false);
        }

        public void requeryBucket(Bucket<Todo> todos) {
            swapCursor(Todo.queryAll(todos).execute());
        }

        public Todo getItem(int position) {
            Bucket.ObjectCursor<Todo> cursor = (Bucket.ObjectCursor<Todo>) super.getItem(position);
            return cursor.getObject();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Bucket.ObjectCursor<Todo> bucketCursor = (Bucket.ObjectCursor<Todo>) cursor;
            Todo todo = bucketCursor.getObject();

            TextView textView = (TextView) view.findViewById(R.id.label);
            textView.setText(todo.getTitle());

            CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            checkbox.setChecked(todo.isChecked());

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // construct a view
            return getLayoutInflater().inflate(R.layout.todo_row, null);
        }

    }

}