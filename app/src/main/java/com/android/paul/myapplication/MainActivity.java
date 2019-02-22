package com.android.paul.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyAsyncTask<String> myAsyncTask = new MyAsyncTask<String>() {
            @Override
            public void preExecute() {
                Log.d("preExecute", Thread.currentThread().getName());
            }

            @Override
            public String doInBackground() {
                Log.d("do in background", Thread.currentThread().getName());
                try {
                    Log.d("start sleep", Thread.currentThread().getName());
                    Thread.sleep(5000);
                    Log.d("finish sleep", Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "do in background";
            }

            @Override
            public void onPostExecute() {
                Log.d("onPostExecute", Thread.currentThread().getName());
            }
        };
        myAsyncTask.execute();
    }
}
