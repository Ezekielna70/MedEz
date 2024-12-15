#include <Arduino.h>
#include <time.h>
#include <WiFi.h>
#include <Wire.h>
#include <RTClib.h>
#include <WebServer.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_NeoPixel.h>

#define NUM_LEDS 13
#define TOUCH_PIN 34
#define WS2812_PIN 4
#define BUZZER_PIN 26

#define RTC_SDA 22
#define RTC_SCL 21
#define OLED_SDA 32
#define OLED_SCL 25
#define MOTOR1_PIN 33
#define MOTOR2_PIN 18
#define MOTOR3_PIN 5
#define SENSOR_OUT_PIN 15
#define CUP_SENSOR_OUT_PIN 27

#define NOTE_D5 587
#define NOTE_E5 659
#define NOTE_G5 784
#define NOTE_A5 880
#define NOTE_B5 988
#define NOTE_C6 1047
#define NOTE_D6 1175

#define DEBUG_MODE true
#if DEBUG_MODE
    #define DEBUG_PRINT(x) Serial.println(x)
#else
    #define DEBUG_PRINT(x)
#endif

WebServer server(80);
String deviceID = "12345";
#define SSID_KEY "wifi_ssid"
#define PASS_KEY "wifi_pass"
#define SCHEDULES_KEY "schedules"
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 25200; // GMT+7
const int daylightOffset_sec = 0;
const char* base_url = "https://greatly-closing-monitor.ngrok-free.app";

RTC_DS1307 rtc;
Preferences preferences;
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_LEDS, WS2812_PIN, NEO_GRB + NEO_KHZ800);
TwoWire WireOLED = TwoWire(1);
Adafruit_SSD1306 display(128, 64, &WireOLED, -1);

int motorPins[] = {MOTOR1_PIN, MOTOR2_PIN, MOTOR3_PIN};

unsigned long lastRefetchTime = 0;
const unsigned long refetchInterval = 3 * 60 * 1000;
unsigned long lastPOST = 0;

String medIds[3];
String medTimes[3][3];
int medRemaining[3];
int medDosage[3] = {0,0,0};
int medTimeCount[3] = {0,0,0};

int alarmMotorID = -1;        
unsigned long lastAlarmTime = 0; 
unsigned long alarmStartTime = 0; 
const unsigned long maxAlarmDuration = 5 * 60 * 1000;

bool alarmActive = false; 
bool isMedicineDispensed = false;
bool scheduleProcessed[3][3] = {false};
bool activeAlarms[3] = {false, false, false};

String customSSID;
String customPassword;
String savedSchedules;

void connectToWiFi();
void startAPMode();
void syncTimeWithNTP();
void startupSound();
void playTone(int frequency, int duration);
void saveWiFiCredentials(const char* ssid, const char* password);

void fetchSchedules();
String loadSchedules();
void checkAndDispense();
void DispenseMed(int motorID);
void saveSchedules(const String& schedules);

void setup() {
  Serial.begin(115200);
  preferences.begin("medez", false);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(MOTOR1_PIN, OUTPUT);
  pinMode(MOTOR2_PIN, OUTPUT);
  pinMode(MOTOR3_PIN, OUTPUT);
  pinMode(TOUCH_PIN, INPUT); 
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(MOTOR1_PIN, LOW);
  digitalWrite(MOTOR2_PIN, LOW);
  digitalWrite(MOTOR3_PIN, LOW);
  pinMode(SENSOR_OUT_PIN, INPUT);
  pinMode(CUP_SENSOR_OUT_PIN, INPUT);
  strip.begin();
  strip.show();

  Wire.begin(RTC_SDA, RTC_SCL);
  if (!rtc.begin()) {
    DEBUG_PRINT("Couldn't find RTC");
    while (1);
  }

  WireOLED.begin(OLED_SDA, OLED_SCL);
  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    DEBUG_PRINT(F("SSD1306 allocation failed"));
    while(1);
  }
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0,0);
  display.setTextSize(2);
  display.println("MedEZ!\norganizingmeds  made easy");
  display.display();
  display.setTextSize(1);

  customSSID = preferences.getString(SSID_KEY, "");
  customPassword = preferences.getString(PASS_KEY, "");
  connectToWiFi();
  syncTimeWithNTP();

  fetchSchedules();
  savedSchedules = loadSchedules();
  if (savedSchedules.length() > 0) {
    DEBUG_PRINT("Loaded saved schedules:");
    DEBUG_PRINT(savedSchedules);
  }

  startupLED();
  startupSound();
}

void loop() {
  DateTime now = rtc.now();

  display.clearDisplay();
  display.setCursor(0, 0);
  display.printf("Time: %02d:%02d:%02d\n\n", now.hour(), now.minute(), now.second());

  if (WiFi.status() != WL_CONNECTED) {
    // Offline Mode
    display.setTextSize(2);
    display.println("OFFLINE\nMODE");
    display.setTextSize(1);
    display.println("Schedules loaded");
    display.println("from saved data.");
    display.display();

    handleAlarm();
    checkAndDispense();
  } else {
    // Online Mode
    if (millis() - lastRefetchTime >= refetchInterval) {
      DEBUG_PRINT("Refetching schedules...");
      fetchSchedules();
      lastRefetchTime = millis();
    }
    display.setCursor(0, 43);
    display.println("Medicine Count:");
    display.setCursor(0, 16);

    for (int i = 0; i < 3; i++) {
      display.setCursor(0, 16 + (i * 10));
      String lineText = String(i + 1) + " : ";
      for (int t = 0; t < medTimeCount[i]; t++) {
        lineText += medTimes[i][t];
        if (t < medTimeCount[i] - 1) lineText += ", ";
      }
      display.println(lineText);
    }

    display.setCursor(0, 53);
    String remainingText = "1: " + String(medRemaining[0]) + " | " +
                          "2: " + String(medRemaining[1]) + " | " +
                          "3: " + String(medRemaining[2]);
    display.println(remainingText);
    display.display();

    handleAlarm();
    checkAndDispense();
    handleCupEvent();
  }

  int touchValue = digitalRead(TOUCH_PIN);
  if (touchValue == HIGH) {
    DEBUG_PRINT("Touch detected. Dispensing all active medicines.");
    for (int i = 0; i < 3; i++) {
      if (activeAlarms[i]) {
        DispenseMed(i + 1);
        activeAlarms[i] = false;
        isMedicineDispensed = true;
      }
    }
  }

  delay(1000);
}

void playTone(int frequency, int duration) {
  tone(BUZZER_PIN, frequency, duration);
  delay(duration);
}

void startupSound() {
  playTone(NOTE_D5, 150); 
  playTone(NOTE_E5, 150); 
  playTone(NOTE_G5, 150); 
  playTone(NOTE_A5, 150);  
  playTone(NOTE_B5, 150);  
  playTone(NOTE_G5, 150);  
  delay(300);
  playTone(NOTE_E5, 150);  
  playTone(NOTE_G5, 150);  
  playTone(NOTE_A5, 150);  
  playTone(NOTE_D6, 300);  
  delay(300);
  playTone(NOTE_C6, 300);  
}

void DispenseMed(int motorID) {
  if (motorID < 1 || motorID > 3) {
    DEBUG_PRINT("Invalid motor ID");
    return;
  }

  int motorIndex = motorID - 1;
  int maxSteps = 20;
  int stepDuration = 15;
  int dosage = medDosage[motorIndex];

  if (dosage <= 0) {
    Serial.printf("Invalid dosage for motor %d\n", motorID);
    return;
  }

  Serial.printf("Dispensing %d doses from motor %d...\n", dosage, motorID);

  for (int d = 0; d < dosage; d++) {
    bool objectDetected = false;

    for (int step = 0; step < maxSteps; step++) {
      digitalWrite(motorPins[motorIndex], HIGH);
      delay(stepDuration);
      digitalWrite(motorPins[motorIndex], LOW);

      int sensorVal = digitalRead(SENSOR_OUT_PIN);
      if (sensorVal == LOW) {
        Serial.printf("Object detected by IR sensor on step %d\n", step + 1);
        objectDetected = true;
        break;
      }
      delay(200);
    }
    if (!objectDetected) {
      Serial.printf("Dosage %d for motor %d not fully dispensed!\n", d + 1, motorID);
    } else {
      Serial.printf("Dosage %d for motor %d dispensed successfully.\n", d + 1, motorID);
    }
    delay(500);
  }

  if (medRemaining[motorIndex] >= dosage) {
    medRemaining[motorIndex] -= dosage;
    Serial.printf("Medicine remaining for motor %d: %d\n", motorID, medRemaining[motorIndex]);
  } else {
    Serial.printf("Not enough medicine remaining for motor %d\n", motorID);
  }
}

void syncTimeWithNTP() {
  struct tm timeinfo;
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

    if (getLocalTime(&timeinfo)) {
      DEBUG_PRINT("Time synchronized with NTP");
      rtc.adjust(DateTime(timeinfo.tm_year + 1900, timeinfo.tm_mon + 1, timeinfo.tm_mday, timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec));
      return;
    }

  DEBUG_PRINT("Failed to obtain time from NTP, using RTC time");
  DateTime now = rtc.now();
  rtc.adjust(now);
  DEBUG_PRINT("RTC time used: " + String(now.year()) + "-" + String(now.month()) + "-" + String(now.day()) + " " + 
               String(now.hour()) + ":" + String(now.minute()) + ":" + String(now.second()));
}

void fetchSchedules() {
  const int maxRetries = 3;       
  const int retryDelay = 5000;    
  int attempt = 0;

  for (int i = 0; i < 3; i++) {
    for (int t = 0; t < 3; t++) {
      scheduleProcessed[i][t] = false;
    }
    medDosage[i] = 0;
    medRemaining[i] = 0;
    medTimeCount[i] = 0;
  }

  String url = String(base_url) + "/getMed/" + deviceID;

  while (attempt < maxRetries) {
    HTTPClient http;
    http.begin(url);
    int httpCode = http.GET();

    if (httpCode == 200) {
      String payload = http.getString();
      DEBUG_PRINT("API Response:");
      DEBUG_PRINT(payload);

      saveSchedules(payload);

      StaticJsonDocument<1024> doc;
      DeserializationError error = deserializeJson(doc, payload);
      if (error) {
        Serial.print("JSON parse failed: ");
        DEBUG_PRINT(error.f_str());
        http.end();
        return;
      }

      const char* status = doc["status"];
      if (strcmp(status, "success") == 0) {
        JsonArray medicines = doc["medicines"];
        for (JsonObject med : medicines) {
          int medSlot = med["med_slot"];
          if (medSlot < 1 || medSlot > 3) {
            Serial.printf("Invalid med_slot: %d\n", medSlot);
            continue;
          }

          int slotIndex = medSlot - 1; // Convert 1-based slot to 0-based index
          medIds[slotIndex] = (const char*)med["med_id"];
          medRemaining[slotIndex] = med["med_remaining"];
          medDosage[slotIndex] = med["med_dosage"];
          JsonArray times = med["consumption_times"];
          medTimeCount[slotIndex] = times.size() < 3 ? times.size() : 3;
          for (int t = 0; t < medTimeCount[slotIndex]; t++) {
            medTimes[slotIndex][t] = (const char*)times[t];
            Serial.printf("Med %d, Time %d: %s\n", medSlot, t + 1, medTimes[slotIndex][t].c_str());
          }
        }
        http.end();
        return;
      } else {
        DEBUG_PRINT("API returned status not success");
      }
    } else {
      Serial.printf("HTTP GET failed with code: %d\n", httpCode);
    }

    http.end();
    attempt++;
    if (attempt < maxRetries) {
      DEBUG_PRINT("Retrying HTTP request...");
      delay(retryDelay);
    } else {
      DEBUG_PRINT("Max retries reached. Using saved schedules...");
      String savedSchedules = loadSchedules();
      if (savedSchedules.length() > 0) {
        DEBUG_PRINT("Using saved schedules:");
        DEBUG_PRINT(savedSchedules);

        StaticJsonDocument<1024> doc;
        DeserializationError error = deserializeJson(doc, savedSchedules);
        if (error) {
          Serial.print("Failed to parse saved schedules: ");
          DEBUG_PRINT(error.f_str());
          return;
        }

        JsonArray medicines = doc["medicines"];
        for (JsonObject med : medicines) {
          int medSlot = med["med_slot"];
          if (medSlot < 1 || medSlot > 3) {
            Serial.printf("Invalid med_slot: %d\n", medSlot);
            continue;
          }

          int slotIndex = medSlot - 1;
          medIds[slotIndex] = (const char*)med["med_id"];
          medRemaining[slotIndex] = med["med_remaining"];
          medDosage[slotIndex] = med["med_dosage"];
          JsonArray times = med["consumption_times"];
          medTimeCount[slotIndex] = times.size() < 3 ? times.size() : 3;
          for (int t = 0; t < medTimeCount[slotIndex]; t++) {
            medTimes[slotIndex][t] = (const char*)times[t];
            Serial.printf("Loaded Med %d, Time %d: %s\n", medSlot, t + 1, medTimes[slotIndex][t].c_str());
          }
        }
      } else {
        DEBUG_PRINT("No saved schedules found.");
      }
    }
  }
}

void handleCupEvent() {
  static bool cupPresent = true;
  bool currentCupState = digitalRead(CUP_SENSOR_OUT_PIN) == LOW;

  if (cupPresent && !currentCupState) {
    DEBUG_PRINT("Medicine cup removed!");
    cupPresent = false;
  } else if (!cupPresent && currentCupState) {
    DEBUG_PRINT("Medicine cup reinserted!");
    cupPresent = true;

    for (int i = 0; i < 3; i++) {
      if (activeAlarms[i] && isMedicineDispensed) {
        Serial.printf("Cup reinserted. Marking medicine from motor %d as taken.\n", i + 1);
        postMedicineStatus("taken", i + 1);
      }
    }
  }
}

void handleAlarm() {
  bool anyAlarmActive = false;
  for (int i = 0; i < 3; i++) {
    if (activeAlarms[i]) {
      anyAlarmActive = true;
      break;
    }
  }

  if (!anyAlarmActive) {
    normalLED();
    return;
  }

  unsigned long currentMillis = millis();

  if (currentMillis - alarmStartTime >= maxAlarmDuration) {
    DEBUG_PRINT("Alarm duration exceeded. Marking medicines as not taken.");
    for (int i = 0; i < 3; i++) {
      if (activeAlarms[i]) {
        postMedicineStatus("not taken", i + 1); // Send not taken for each active alarm
        activeAlarms[i] = false; // Reset the alarm
      }
    }
    return;
  }

  alarmLED();

  if (currentMillis - lastAlarmTime >= 500) {
    tone(BUZZER_PIN, NOTE_A5, 300);
    lastAlarmTime = currentMillis;
  }

  display.clearDisplay();
  display.setCursor(0, 0);
  display.println("ALARM!");
  for (int i = 0; i < 3; i++) {
    if (activeAlarms[i]) {
      display.printf("Take Medicine %d\n", i + 1);
    }
  }
  display.display();

  DEBUG_PRINT("Alarms active.");
}

void checkAndDispense() {
  DateTime now = rtc.now();
  int currentHour = now.hour();
  int currentMinute = now.minute();

  for (int i = 0; i < 3; i++) {
    for (int t = 0; t < medTimeCount[i]; t++) {
      String timeStr = medTimes[i][t];
      int colonPos = timeStr.indexOf(':');
      if (colonPos > 0) {
        int hour = timeStr.substring(0, colonPos).toInt();
        int minute = timeStr.substring(colonPos + 1).toInt();

        if (hour == currentHour && minute == currentMinute && !activeAlarms[i] && !scheduleProcessed[i][t]) {
          activeAlarms[i] = true;        
          alarmStartTime = millis();     
          isMedicineDispensed = false; 
          scheduleProcessed[i][t] = true;
          Serial.printf("Alarm triggered for motor %d\n", i + 1);
        }
      }
    }
  }
}

void postMedicineStatus(const char* status, int motorID) {
  HTTPClient http;
  String url = String(base_url) + "/medStatus";

  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  StaticJsonDocument<256> doc;
  doc["dev_id"] = deviceID;
  doc["med_id"] = medIds[motorID - 1];
  doc["status"] = status;

  String requestBody;
  serializeJson(doc, requestBody);

  int httpCode = http.POST(requestBody);
  if (httpCode > 0) {
    Serial.printf("POST Response for motor %d: %d\n", motorID, httpCode);
    DEBUG_PRINT(http.getString());
  } else {
    Serial.printf("POST failed for motor %d, error: %s\n", motorID, http.errorToString(httpCode).c_str());
  }

  http.end();
}

void setLEDColor(uint32_t color) {
  for (int i = 0; i < NUM_LEDS; i++) {
    strip.setPixelColor(i, color);
  }
  strip.show();
}

void setAllLEDs(uint32_t color) {
  for (int i = 0; i < NUM_LEDS; i++) {
    strip.setPixelColor(i, color);
  }
  strip.show();
}

void alarmLED() {
  static bool ledState = false;
  uint32_t color = ledState ? strip.Color(255, 0, 0) : strip.Color(0, 0, 0);
  setAllLEDs(color);
  ledState = !ledState;
}

void normalLED() {
  setAllLEDs(strip.Color(0, 0, 255));
}

void startupLED() {
  for (int i = 0; i < NUM_LEDS; i++) {
    strip.setPixelColor(i, strip.Color(0, 0, 255));
    strip.show();
    delay(150);
  }
  setAllLEDs(strip.Color(0, 0, 255)); 
}

void pulseOrangeLED() {
  static int brightness = 0;
  static int fadeAmount = 5;
  static unsigned long lastUpdate = 0;
  const int updateInterval = 30;

  if (millis() - lastUpdate >= updateInterval) {
    brightness += fadeAmount;

    if (brightness <= 0 || brightness >= 255) {
      fadeAmount = -fadeAmount;
    }

    uint8_t red = brightness;    
    uint8_t green = brightness / 6; 
    uint8_t blue = 0;

    for (int i = 0; i < NUM_LEDS; i++) {
      strip.setPixelColor(i, strip.Color(red, green, blue));
    }
    strip.show();

    lastUpdate = millis();
  }
}

void saveSchedules(const String& schedules) {
  preferences.putString(SCHEDULES_KEY, schedules);
  DEBUG_PRINT("Schedules saved persistently.");
}

String loadSchedules() {
  return preferences.getString(SCHEDULES_KEY, "");
}

void connectToWiFi() {
  Serial.printf("Attempting to connect to saved Wi-Fi: %s\n", customSSID.c_str());
  WiFi.begin(customSSID.c_str(), customPassword.c_str());

  unsigned long startAttemptTime = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 10000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    DEBUG_PRINT("\nConnected to Wi-Fi.");
  } else {
    DEBUG_PRINT("\nFailed to connect to Wi-Fi. Starting Access Point...");
    startAPMode();

    if (WiFi.status() != WL_CONNECTED) {
      startOfflineMode();
    }
  }
}


void saveWiFiCredentials(const char* ssid, const char* password) {
  preferences.putString(SSID_KEY, ssid);
  preferences.putString(PASS_KEY, password);
  DEBUG_PRINT("WiFi credentials saved.");
}

void startOfflineMode() {
  DEBUG_PRINT("Switching to OFFLINE mode...");
  
  // Load schedules from preferences
  savedSchedules = loadSchedules();
  if (savedSchedules.length() > 0) {
    DEBUG_PRINT("Loaded saved schedules for offline mode:");
    DEBUG_PRINT(savedSchedules);
  } else {
    DEBUG_PRINT("No saved schedules found for offline mode.");
  }

  display.clearDisplay();
  display.setCursor(0, 0);
  display.setTextSize(2);
  display.println("OFFLINE MODE");
  display.setTextSize(1);
  display.println("Please Wait!!");
  display.println("Reading schedules");
  display.println("from saved data...");
  display.display();
}

#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>

void startAPMode() {
  DEBUG_PRINT("Starting Access Point Mode...");

  // Scan for WiFi networks before starting AP
  int numNetworks = WiFi.scanNetworks();
  String networkOptions = "";
  for (int i = 0; i < numNetworks; i++) {
    networkOptions += "<option value='" + WiFi.SSID(i) + "'>" + WiFi.SSID(i) + 
                      " (Signal: " + WiFi.RSSI(i) + " dBm)" + "</option>";
  }

  // Set a static IP address for the Access Point
  IPAddress local_IP(192, 168, 4, 1);
  IPAddress gateway(192, 168, 4, 1);
  IPAddress subnet(255, 255, 255, 0);
  WiFi.softAPConfig(local_IP, gateway, subnet);

  // Start the Access Point
  if (WiFi.softAP("MedEZ_Config", "12345678", 1, 0, 4)) {
    DEBUG_PRINT("Access Point started successfully.");
  } else {
    DEBUG_PRINT("Failed to start Access Point.");
    return;
  }

  IPAddress IP = WiFi.softAPIP();
  Serial.print("Access Point IP: ");
  DEBUG_PRINT(IP);

  display.clearDisplay();
  display.setCursor(0, 0);
  display.setTextSize(1);
  display.println("Connect to Wi-Fi:");
  display.println("SSID: MedEZ_Config");
  display.println("Pass: 12345678\n");
  display.println("Open browser:");
  display.print("http://");
  display.println(IP);
  display.display();

 server.on("/", HTTP_GET, [networkOptions]() {
    String html = R"(
      <!DOCTYPE html>
      <html>
      <head>
        <title>MedEZ WiFi Configuration</title>
        <meta name='viewport' content='width=device-width, initial-scale=1'>
        <style>
          body { font-family: Arial, sans-serif; max-width: 400px; margin: 0 auto; padding: 20px; }
          select, input, button { width: 100%; padding: 10px; margin: 10px 0; }
          h1 { text-align: center; color: #333; }
          .restart-btn { background-color: #f44336; color: white; border: none; }
          .config-btn { background-color: #4CAF50; color: white; border: none; }
          .btn-container { display: flex; gap: 10px; }
        </style>
      </head>
      <body>
        <h1>WiFi Configuration</h1>
        <p><strong>Device ID:</strong> )" + deviceID + R"(</p>
        <form method='POST' action='/setWiFi'>
          <label for='ssid'>Select WiFi Network:</label>
          <select name='ssid' id='ssid' required>
            <option value=''>Choose a Network</option>
            )" + networkOptions + R"(
            <option value='other'>Enter Other Network</option>
          </select>
          
          <div id='otherNetwork' style='display:none;'>
            <label for='customSSID'>Custom Network Name:</label>
            <input type='text' name='customSSID' id='customSSID' placeholder='Enter Network Name'>
          </div>
          
          <label for='password'>Password:</label>
          <input type='password' name='password' id='password' required>
          
          <div class='btn-container'>
            <button type='submit' class='config-btn'>Save Configuration</button>
          </div>
        </form>

        <form method='POST' action='/restart' style='margin-top: 20px;'>
          <button type='submit' class='restart-btn'>Restart Device</button>
        </form>

        <script>
          document.getElementById('ssid').addEventListener('change', function() {
            var otherNetwork = document.getElementById('otherNetwork');
            otherNetwork.style.display = (this.value === 'other') ? 'block' : 'none';
            
            if (this.value !== 'other') {
              document.getElementById('customSSID').value = '';
            }
          });
        </script>
      </body>
      </html>
    )";
    server.send(200, "text/html", html);
});


  server.on("/setWiFi", HTTP_POST, []() {
    String ssid = server.arg("ssid");
    String password = server.arg("password");
    
    // If 'other' is selected, use the custom SSID
    if (ssid == "other") {
      ssid = server.arg("customSSID");
    }

    if (!ssid.isEmpty() && !password.isEmpty()) {
      saveWiFiCredentials(ssid.c_str(), password.c_str());

      String responseHtml = R"(
        <!DOCTYPE html>
        <html>
        <head>
          <title>Configuration Saved</title>
          <meta name='viewport' content='width=device-width, initial-scale=1'>
          <style>
            body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }
            .success { color: green; }
          </style>
        </head>
        <body>
          <h1 class='success'>Credentials Saved!</h1>
          <p>Device will restart and connect to the network.</p>
          <script>
            setTimeout(function() {
              window.location.href = '/';
            }, 3000);
          </script>
        </body>
        </html>
      )";
      
      server.send(200, "text/html", responseHtml);
      delay(3000);
      ESP.restart();
    } else {
      server.send(400, "text/html", "<h1>Missing SSID or Password!</h1>");
    }
  });

  // Add a new route to handle device restart
  server.on("/restart", HTTP_POST, []() {
    String responseHtml = R"(
      <!DOCTYPE html>
      <html>
      <head>
        <title>Restarting Device</title>
        <meta name='viewport' content='width=device-width, initial-scale=1'>
        <style>
          body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }
          .restart { color: orange; }
        </style>
      </head>
      <body>
        <h1 class='restart'>Restarting Device...</h1>
        <p>The device will restart momentarily.</p>
        <script>
          setTimeout(function() {
            window.location.href = '/';
          }, 5000);
        </script>
      </body>
      </html>
    )";
    
    server.send(200, "text/html", responseHtml);
    delay(1000);
    ESP.restart();
  });

  server.begin();
  DEBUG_PRINT("Web Server started.");
  
  unsigned long startTime = millis();
  while (true) {
    server.handleClient();
    pulseOrangeLED();
    if (millis() - startTime > 300000) { // 5 minutes timeout
      DEBUG_PRINT("Timeout: Switching to offline mode...");
      startOfflineMode();
      break;
    }
  }
}

