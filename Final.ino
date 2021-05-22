#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <EEPROM.h>
#include <cstdlib>

const char* ssid = "HappyHome";
const char* password = "145789632*";
const char* mqtt_server = "192.168.1.100";
int pins[4]={14,12,13,15};

WiFiClient espClient;
PubSubClient client(espClient);

void callback(char* topic, byte* payload, unsigned int length) {
  byte msg[length];

  for(int i=0; i<length/2; i++){
    int devId=(int)payload[i] - 48;
    int state=(int)payload[(length/2)+i] -48;
    doSwitch(devId,state);
  }

  for (int i = 0; i < length; i++)
    msg[i]=payload[i];

  client.publish("post/extra", msg, length, true);
}

void setPinMode(){
  pinMode(pins[0], OUTPUT);
  pinMode(pins[1], OUTPUT);
  pinMode(pins[2], OUTPUT);
  pinMode(pins[3], OUTPUT);
}

void initFromROM(){
  EEPROM.begin(512);
  for(int i=0; i<4; i++){
    Serial.print((EEPROM.read(i)));
    digitalWrite(pins[i], (EEPROM.read(i))==0);
  }
}

void setup_wifi() {
  delay(10);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  randomSeed(micros());
}

void reconnect() {
  while (!client.connected()) {
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      client.subscribe("get/extra");
    } else {
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(9600);
  while(! Serial);
  setPinMode();
  initFromROM();
  setup_wifi();

  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void loop() {

  if (!client.connected()) {
    reconnect();
  }
  client.loop();
}

void doSwitch(int devId, int state){

  Serial.print(devId);
  Serial.print(" ");
  Serial.println(state);

  EEPROM.write(devId, state);
  EEPROM.commit();
  digitalWrite(pins[devId], state==0);
}