package com.example.smartswitch;

import android.content.Context;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;

public class Common {
    private static final Common DataHolder = new Common();
    private MqttAndroidClient Client;

    public static Common getInstance() {
        return DataHolder;
    }

    public MqttAndroidClient getClient() {
        return Client;
    }

    public void setClient(MqttAndroidClient client) {
        this.Client = client;
    }

    public void Toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
