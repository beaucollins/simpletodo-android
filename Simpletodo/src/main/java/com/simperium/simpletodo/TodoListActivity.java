package com.simperium.simpletodo;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.ForegroundColorSpan;
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

    public static final String EMPTY_STRING="";

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

        // we don't create blank tasks, but leave the keyboard
        if (label.equals(EMPTY_STRING))
            return true;

        tv.getEditableText().clear();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0x0);

        Todo todo = mTodos.newObject();
        todo.setTitle(label);
        todo.setOrder(getListView().getAdapter().getCount());
        todo.save();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
        Todo todo = mAdapter.getItem(position);
        todo.toggleDone();
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkbox);
        checkbox.setChecked(todo.isDone());
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

            CharSequence title = todo.getTitle();

            if (title.equals(EMPTY_STRING))
                title = emptyTitle();

            textView.setText(title);

            CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            checkbox.setChecked(todo.isDone());

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // construct a view
            return getLayoutInflater().inflate(R.layout.todo_row, null);
        }

    }

    protected CharSequence emptyTitle() {
        SpannableString title = new SpannableString(getString(R.string.empty_task_title));
        int length = title.length();
        title.setSpan(new StyleSpan(Typeface.ITALIC), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.empty_task_text_color)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return title;
    }

}