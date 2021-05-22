package com.example.smartswitch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final CardView[] mainCards = new CardView[4];
    private final CardView[] extraCards = new CardView[4];
    public Switch[] mainSwitches = new Switch[4];
    public Switch[] extraSwitches = new Switch[4];
    Context context;
    Common Util;
    MqttAndroidClient client;
    private HashMap<View, Switch> GetSwitch;

    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
    }

    public void initializeComponents() {
        context = this;
        Util = Common.getInstance();
        client = Util.getClient();

        mainCards[0] = findViewById(R.id.card_cfl);
        mainCards[1] = findViewById(R.id.card_backlights);
        mainCards[2] = findViewById(R.id.card_fan);
        mainCards[3] = findViewById(R.id.card_desklights);

        extraCards[0] = findViewById(R.id.card_mainssocket);
        extraCards[1] = findViewById(R.id.card_upssocket);
        extraCards[2] = findViewById(R.id.card_singlelight);
        extraCards[3] = findViewById(R.id.card_outerbulb);

        mainSwitches[0] = findViewById(R.id.switch_cfl);
        mainSwitches[1] = findViewById(R.id.swith_backlights);
        mainSwitches[2] = findViewById(R.id.switch_fan);
        mainSwitches[3] = findViewById(R.id.switch_desklights);

        extraSwitches[0] = findViewById(R.id.switch_mainssocket);
        extraSwitches[1] = findViewById(R.id.switch_upssocket);
        extraSwitches[2] = findViewById(R.id.switch_singlelight);
        extraSwitches[3] = findViewById(R.id.switch_outerbulb);

        GetSwitch = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            GetSwitch.put(mainCards[i], mainSwitches[i]);
            GetSwitch.put(extraCards[i], extraSwitches[i]);
        }

        registerCallbacks();
        subscribeToTopic();
    }

    private void registerCallbacks() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Intent intent = new Intent(context, SplashScreen.class);
                context.startActivity(intent);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                parseMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("Info", "Message Delivered!");
            }
        });
    }

    private void subscribeToTopic() {
        String[] topics = {"status/main", "status/extra"};
        try {
            client.subscribe(topics, new int[]{0, 0});
        } catch (Exception e) {
            Log.e("Subscription Error", e.getMessage());
        }
    }

    private void parseMessage(String topic, MqttMessage mqttMessage) {
        byte[] msg = mqttMessage.getPayload();

        if (msg.length != 4) {
            Util.Toast(context, "Invalid Data Received");
            return;
        }

        if (topic.equals("status/main"))
            for (int i = 0; i < 4; i++)
                mainSwitches[i].setChecked(msg[i] == '1');
        else if (topic.equals("status/extra"))
            for (int i = 0; i < 4; i++)
                extraSwitches[i].setChecked(msg[i] == '1');
    }

    public void publishToTopic(String topic, String payload) {
        try {
            client.publish(topic, payload.getBytes(), 1, false);
        } catch (MqttException e) {
            Util.Toast(context, "Not Switched!");
            Log.d("Error", e.toString());
        }
    }

    public void cardHandler(View v) {
        Switch clickedSwitch = GetSwitch.get(v);
        switchHandler(clickedSwitch);
    }

    public void switchHandler(View v) {
        Switch clickedSwitch = (Switch) v;
        String tag = clickedSwitch.getTag().toString();
        String[] data = tag.split(" ");
        String topic = data[0];
        String payload = data[1];
        payload += clickedSwitch.isChecked() ? "0" : "1";

        publishToTopic(topic, payload);
    }

    @Override
    public void onBackPressed() {
        if (isBackPressed) {
            finishAffinity();
        } else {
            Util.Toast(context, "Press back again to exit!");
            isBackPressed = !isBackPressed;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.exit(0);
    }
}