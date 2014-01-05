package com.simperium.android;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.simperium.Simperium;
import com.simperium.client.Bucket;
import com.simperium.client.BucketNameInvalid;

import android.util.Log;

public abstract class SimperiumService extends Service {

    public static final String TAG = "Simperium.SimperiumService";

    /**
     * Subclasses implement this method and configure their simperium instance there
     */
    abstract public Simperium initializeSimperium();

    /**
     * Interface for objects that will bind to the SimperiumService
     * @see SimperiumService.bind
     */
    public interface SimperiumConnector {

        public Class getServiceClass();

        /**
         * Called whent he SimperiumService has been bound and is ready
         */
        public void onSimperiumConnected(SimperiumService service);

        /**
         * Called when SimperiumService has been disconnected
         */
        public void onSimperiumDisconnected();
    }

    
    public class SimperiumBinder extends Binder {

        public SimperiumService getService() {
            return SimperiumService.this;
        }

    }

    /**
     * Bind the provided connector to SimperiumService. Returns the connection
     * needed to unbind the service.
     */
    public static <T extends SimperiumService> ServiceConnection bind(Context context, final SimperiumConnector connector) {

        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SimperiumBinder binder = (SimperiumBinder) service;
                connector.onSimperiumConnected((T) binder.getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                connector.onSimperiumDisconnected();
            }

        };

        context.bindService(new Intent(context, connector.getServiceClass()), connection, BIND_AUTO_CREATE);

        return connection;

    }

    protected Simperium mSimperium;


    public Simperium getSimperium(){
        if (mSimperium == null) {
            mSimperium = initializeSimperium();
        }
        return mSimperium;
    }

    @Override
    public void onCreate() {

        android.util.Log.d("Simpletodo", "Creating service");
        // initialize simperium
        getSimperium();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        android.util.Log.d("Simpletodo", "Destroying service");
    }


    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    private final SimperiumBinder mBinder = new SimperiumBinder();

    public static abstract class Activity<T extends SimperiumService> extends android.app.Activity
    implements SimperiumConnector {

        SimperiumService mService;

        private ServiceConnection mConnection ;

        public SimperiumService getSimperiumService() {
            if (mService == null) {
                throw new RuntimeException("Attempting to access SimperiumService before it is bound");
            }
            return mService;
        }

        public Simperium getSimperium() {
            return getSimperiumService().getSimperium();
        }

        @Override
        public void onSimperiumConnected(SimperiumService service) {
            mService = service;
        }

        @Override
        public void onSimperiumDisconnected() {
            // do nothing
        }

        @Override
        public void onResume() {
            super.onResume();
            mConnection = SimperiumService.bind(this, this);
        }

        @Override
        public void onPause() {
            unbindService(mConnection);
            super.onPause();
        }

    }

    public static abstract class ListActivity extends android.app.ListActivity
    implements SimperiumConnector {

        SimperiumService mService;

        private ServiceConnection mConnection ;

        public SimperiumService getSimperiumService() {
            if (mService == null) {
                throw new RuntimeException("Attempting to access SimperiumService before it is bound");
            }
            return mService;
        }

        public Simperium getSimperium() {
            return getSimperiumService().getSimperium();
        }

        @Override
        public void onSimperiumConnected(SimperiumService service) {
            mService = service;
        }

        @Override
        public void onSimperiumDisconnected() {
            // subclass should override
        }

        @Override
        public void onResume() {
            super.onResume();
            mConnection = SimperiumService.bind(this, this);
        }

        @Override
        public void onPause() {
            unbindService(mConnection);
            super.onPause();
        }

    }
}