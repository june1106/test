# How to Test RPI Connection - Complete Guide

## 🔍 **Step-by-Step Testing Process**

### **Step 1: Verify RPI is Paired** ✅
```bash
# Check paired devices
adb shell "dumpsys bluetooth_manager | grep raspberrypi"
# Result: ✅ raspberrypi (AA:AA:AA:AA:AA:AA) PAIRED
```

### **Step 2: Test App Connection**

#### **Method 1: Through App UI**
1. **Open the app** on your device
2. **Go to Bluetooth tab** (bottom navigation)
3. **Click "SCAN"** button
4. **Look for "raspberrypi"** in the device list
5. **Click on "raspberrypi"** to connect
6. **Check status** - should show "CONNECTED"

#### **Method 2: Check Connection Logs**
```bash
# Monitor Bluetooth connection logs
adb logcat -s "AndroidBluetoothRepository" | grep -E "(Connecting|Connected|Failed)"
```

### **Step 3: Test Message Sending**

#### **Test Robot Movement Commands**
1. **Go to Home tab**
2. **Click movement buttons** (FORWARD, LEFT, RIGHT, REVERSE, STOP)
3. **Check Messages tab** - should show "Sent: FORWARD", "Sent: LEFT", etc.

#### **Test Obstacle Commands**
1. **Tap on grid** to place obstacle
2. **Check Messages tab** - should show "Sent: ADD,1,(10,10)"
3. **Long-press obstacle face** to set target
4. **Check Messages tab** - should show "Sent: FACE,1,N"

### **Step 4: Test Message Receiving**

#### **Simulate RPI Messages**
```bash
# Send test message to app (if RPI is connected)
# This would normally come from RPI, but we can test the parsing
```

## 🧪 **What to Look For**

### **✅ Successful Connection Indicators:**
- **Bluetooth tab shows "CONNECTED"**
- **Status shows "Connected" instead of "Not Connected"**
- **Messages tab shows connection messages**
- **Movement buttons send commands successfully**

### **❌ Connection Issues:**
- **Bluetooth tab shows "DISCONNECTED"**
- **Status shows "Not Connected"**
- **No devices found when scanning**
- **Connection timeout or errors**

## 🔧 **Troubleshooting**

### **If RPI Not Found:**
1. **Check RPI Bluetooth is enabled**
2. **Check RPI is in discoverable mode**
3. **Try pairing again from Android settings**

### **If Connection Fails:**
1. **Check RPI is running Bluetooth service**
2. **Check RPI has correct SPP service**
3. **Try disconnecting and reconnecting**

### **If Messages Not Sending:**
1. **Check connection status**
2. **Check Messages tab for error messages**
3. **Verify RPI is receiving data**

## 📱 **Current App Status**

### **✅ What's Working:**
- **RPI is paired** (AA:AA:AA:AA:AA:AA)
- **App has real Bluetooth implementation**
- **Message formats are correct**
- **All UI elements are functional**

### **🧪 What to Test:**
1. **Open app → Bluetooth tab**
2. **Click SCAN → Look for raspberrypi**
3. **Click raspberrypi → Should connect**
4. **Go to Home → Test movement buttons**
5. **Check Messages tab → Should show sent commands**

## 🎯 **Expected Results**

### **Successful Connection:**
- Bluetooth tab shows "CONNECTED"
- Status shows "Connected"
- Movement buttons work
- Messages show sent commands
- Obstacle placement works

### **If Everything Works:**
✅ **App can communicate with RPI**
✅ **All MDP checklist items functional**
✅ **Ready for competition use**

---

## 🚀 **Quick Test Commands**

```bash
# 1. Check RPI is paired
adb shell "dumpsys bluetooth_manager | grep raspberrypi"

# 2. Launch app
adb shell "am start -n com.example.mdpremotecontroller/.SimpleMainActivity"

# 3. Monitor connection logs
adb logcat -s "AndroidBluetoothRepository"

# 4. Check app logs
adb logcat -s "MDPRemoteController"
```

**Follow these steps to verify RPI connection!**



