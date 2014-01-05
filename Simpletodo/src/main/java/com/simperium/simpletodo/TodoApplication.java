package com.simperium.simpletodo;

import android.app.Application;
import android.content.Intent;

public class TodoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        android.util.Log.d("Simpletodo", "Hola mundo");
        startService(new Intent(this, TodoService.class));
    }
}