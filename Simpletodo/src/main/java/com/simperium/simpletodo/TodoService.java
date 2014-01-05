package com.simperium.simpletodo;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.simperium.Simperium;
import com.simperium.android.SimperiumService;
import com.simperium.android.LoginActivity;
import com.simperium.client.Bucket;
import com.simperium.client.BucketNameInvalid;
import com.simperium.client.BucketObject;

public class TodoService extends SimperiumService {

    Bucket<Todo> mTodoBucket;

    @Override
    public Simperium initializeSimperium() {
        // boot up the simperium instance
        android.util.Log.d("Simpletodo", "on create service");
        Simperium simperium = Simperium.newClient(BuildConfig.SIMPERIUM_APP, BuildConfig.SIMPERIUM_KEY, this);

        try {
            mTodoBucket = simperium.bucket("todo", new Todo.Schema());
            mTodoBucket.start();

            if (simperium.needsAuthorization()) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } catch (BucketNameInvalid e) {
            android.util.Log.e("Simpletodo", "Could not set up bucket", e);
        }
        return simperium;
    }

    public Bucket<Todo> getTodoBucket() {
        return mTodoBucket;
    }

}