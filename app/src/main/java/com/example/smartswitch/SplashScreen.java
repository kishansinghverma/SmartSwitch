package com.example.smartswitch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class SplashScreen extends AppCompatActivity {

    MqttConnectOptions cloudConnectOptions;
    MqttConnectOptions localConnectOptions;
    TextView infoText;
    ImageView imageView;
    Boolean isThisActivityVisible;
    Common Util = Common.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        initializeComponents();
        initializeConnectionParams();
        connectToLocal();
    }

    public void initializeComponents() {
        imageView = findViewById(R.id.loader);
        Glide.with(this).load(getApplicationContext().getResources().getDrawable(R.drawable.anim)).into(imageView);
        infoText = findViewById(R.id.info);
        isThisActivityVisible = new Boolean(true);
    }

    public void initializeConnectionParams() {
        cloudConnectOptions = new MqttConnectOptions();
        cloudConnectOptions.setUserName(Configuration.Username);
        cloudConnectOptions.setPassword(Configuration.Password.toCharArray());
        cloudConnectOptions.setCleanSession(true);
        cloudConnectOptions.setConnectionTimeout(5);
        cloudConnectOptions.setAutomaticReconnect(true);

        localConnectOptions = new MqttConnectOptions();
        localConnectOptions.setCleanSession(true);
        localConnectOptions.setConnectionTimeout(1);
    }

    public void connectToLocal() {
        infoText.setText("Trying for local connection...");
        final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), "tcp://192.168.1.100", MqttClient.generateClientId());

        try {
            IMqttToken token = client.connect(localConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Util.Toast(getApplicationContext(), "Connected To Local Server!");
                    Util.setClient(client);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Util.Toast(getApplicationContext(), "Local Connection Failed!");
                    client.unregisterResources();
                    client.close();
                    connectToCloud();
                }
            });
        } catch (MqttException e) {
            Log.d("Error", e.toString());
        }
    }

    public void connectToCloud() {
        infoText.setText("Trying for global connection...");
        final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), Configuration.HostAddress, MqttClient.generateClientId());

        try {
            IMqttToken token = client.connect(cloudConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Util.Toast(getApplicationContext(), "Connected To Cloud Server!");
                    Util.setClient(client);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Util.Toast(getApplicationContext(), "Cloud Connection Failed!");
                    client.unregisterResources();
                    client.close();
                    imageView.setAlpha(0);
                    infoText.setText("Could not connect to any available options...\n Please try after some time.\nError : "+exception.getMessage());
                    if (!isThisActivityVisible) {
                        startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                    }
                }
            });
        } catch (MqttException e) {
            Log.d("Error", e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isThisActivityVisible = false;
    }
}