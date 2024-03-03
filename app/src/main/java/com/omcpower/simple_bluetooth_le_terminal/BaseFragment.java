package com.omcpower.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment implements ServiceConnection, SerialListener {

    protected enum Connected { False, Pending, True }
    protected enum Process {
        Dashboard,TodRead,TodWrite,MaxCurrent,
        Recharge,Balance, DashBoardChannel,Reset,
        MonthlyTariffRecharge,MonthlyTariffBalance
    }
    protected boolean hexEnabled = true;
    protected Connected connected;
    protected String deviceAddress;
    protected SerialService service;
    protected LinearLayout mHome,mConnecting,resetGroup;
    protected ActionBar actionBar;
    protected String deviceName;

    protected int userType;
    private String dName;
    private String dAddress;

    protected TextView receiveText;
    protected TextView sendText;
    protected TextUtil.HexWatcher hexWatcher;
    protected Process process;
    private String cmd,flag;

    protected String rechargeQuery = "";

    protected boolean initialStart = true;

    protected boolean pendingNewline = false;
    protected String newline = TextUtil.newline_crlf;

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                "MyPref", Context.MODE_PRIVATE);
        dName = sharedPref.getString(getString(R.string.deviceName),"noname");
        dAddress = sharedPref.getString(getString(R.string.deviceAddress), "noaddress");
        //Log.d("dname",dName);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * Serial + UI
     */
    protected void connect() {
        try {
            if(connected != Connected.True) {

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                status("connecting...");
                connected = Connected.Pending;
                SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
                service.connect(socket);
            }
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    protected void disconnect() {
        if(connected != Connected.True) {
            connected = Connected.False;
        }
        service.disconnect();
    }

    protected String valueAndCrc(String hexa,int value) {
        String hexString = Integer.toHexString(0x10000 | value).substring(1).toUpperCase();
        String fullHexa = hexa+" "+hexString;
        return hexString+" "+valueAndCrc(fullHexa.replaceAll(" ", ""));
    }

    protected String valueAndCrc(String hex) {
        CRC16Modbus crc = new CRC16Modbus();
        int[] data = hexStringToIntArray(hex);
        for (int d : data) {
            crc.update(d);
        }
        byte[] byteStr = new byte[2];
        byteStr[0] = (byte) ((crc.getValue() & 0x000000ff));
        byteStr[1] = (byte) ((crc.getValue() & 0x0000ff00) >>> 8);
        return String.format("%02X",byteStr[0])+" "+String.format("%02X",byteStr[1]);
    }

    private int[] hexStringToIntArray(String s) {
        int len = s.length()/2;
        int subLenOne = 0;
        int subLenTwo = 2;
        int[] data = new int[len];
        for(int i = 0;i<len;i++) {
            data[i] = Integer.parseInt(s.substring(subLenOne,subLenTwo), 16);
            subLenOne = subLenTwo;
            subLenTwo += 2;
        }
        return data;
    }

    protected void send(String str) {
        //Log.d("No. of Bytes :", "send recharge : "+str);
        cmd = "";
        flag = "";
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        } finally {
            cmd = str;
            flag = getFlag();
            sendToServer(str,flag,1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFlag() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyyHH:mm:ss");
        return myDateObj.format(myFormatObj);
    }

    private void sendToServer(String cmd,String flag,int value) {
        String macAddress = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Api api = RetrofitClient.getClient().create(Api.class);
        Command command = new Command(cmd,macAddress, "",
                flag,value,dName,dAddress);
        Call<Command> call = api.saveCommand(command);
        call.enqueue(new Callback<Command>() {
            @Override
            public void onResponse(Call<Command> call, Response<Command> response) {
                Command myUsersList = response.body();
                if(response.isSuccessful()) {
                    //Toast.makeText(getActivity(), "connected successfully", Toast.LENGTH_LONG).show();
                    /*if(myUsersList.isValid()) {
                        //connectToDevice(position);
                        //Toast.makeText(getActivity(), "valid user : "+myUsersList.isValid(), Toast.LENGTH_LONG).show();
                    } else {
                        //showPopup(macAddress);
                        //Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_LONG).show();
                    }*/
                }

            }

            @Override
            public void onFailure(Call<Command> call, Throwable t) {
                //Toast.makeText(getActivity(), "An error occurred : "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected boolean isWriteProcess = false;
    protected void receive(ArrayDeque<byte[]> datas) {
        // receiveText.append(TextUtil.toHexString(data) + '\n');
        // Log.d("No. of Bytes :", "receive recharge : "+data.length);

        try {
            if(isWriteProcess) {
                showResponse(totalString);
                isWriteProcess = false;
            } else {
                receiveRoute(datas);
            }
        } catch (Exception e) {

        } finally {
            sendToServer(cmd,flag,2);
            cmd = "";
            flag = "";
        }
    }

    protected void showHome() {
        if(userType == 1) {
            mHome.setVisibility(View.VISIBLE);
        }
        resetGroup.setVisibility(View.VISIBLE);
        mConnecting.setVisibility(View.GONE);
        actionBar.setTitle("connected - "+deviceName);
    }

    protected void status(String str) {
        //SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        //spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //receiveText.append(spn);
        if (str == "connected") {
            if(isBackedUp()) {
                showHome();
            }
        } else {
            Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isBackedUp() {
        return true;
    }

    /*
     * SerialListener
     */
    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onSerialRead(ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            //receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    abstract void showResponse(String data);

    protected void sendRoute(String send) {
        String sendStr = replaceBlank(send);
        //Log.d("No. of Bytes :", "send : "+sendStr);
        //Log.d("No. of Bytes :", "send : "+sendStr.length());
        //showResponse(totalString);
        send(sendStr);
    }

    protected int receivedLength = 0;
    protected int totalLength = 0;
    protected String totalString = "";
    protected int lengthToReceive = 0;
    private int dataLength = 0;

    protected void receiveRoute(ArrayDeque<byte[]> datas) {
        for (byte[] data : datas) {
            String s = replaceBlank(TextUtil.toHexString(data, 0, data.length));
            totalString += s;
            receivedLength = s.length();
            totalLength += receivedLength;
            receivedLength = 0;
            if (totalLength >= 6) {
                if (lengthToReceive == 0) {
                    dataLength = getDataLength(totalString);
                    //Log.d("No. of Bytes :", "receive recharge Data : "+totalString);
                    lengthToReceive = 6 + (dataLength * 2) + 4;
                    if (lengthToReceive <= totalString.length()) {
                        String crc = getCrcFromString(totalString, lengthToReceive);
                        String onlyData = getDataFromString(totalString, lengthToReceive);
                        String calculatedCrc = valueAndCrc(onlyData);
                        if (replaceBlank(crc).equals(replaceBlank(calculatedCrc))) {
                            //Log.d("No. of Bytes :", "complete data : "+totalString);
                            //Log.d("No. of Bytes :", "correct crc : "+calculatedCrc);
                        /*
                         crc is correct, data received successfully
                         */
                            showResponse(totalString);
                        } else {
                        /*
                        error in receiving data
                         */
                        }
                        receivedLength = 0;
                        totalLength = 0;
                        totalString = "";
                        lengthToReceive = 0;
                        dataLength = 0;
                    }
                    lengthToReceive = 0;
                }
            }
        }
    }

    protected String dateSeparator(String date) {
        if(date.length() == 6) {
            return date.substring(0,2)+"/"+date.substring(2,4)+"/"+date.substring(4,6);
        } else if(date.length() == 5) {
            return date.substring(0,1)+"/"+date.substring(1,3)+"/"+date.substring(3,5);
        } else {
            return date;
        }
    }

    protected String timeSeparator(String time) {
        if(time.length() == 6) {
            return time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6);
        } else if(time.length() == 5) {
            return time.substring(0,1)+":"+time.substring(1,3)+":"+time.substring(3,5);
        } else {
            return time;
        }
    }

    protected int getDataLength(String received) {
        return Integer.parseInt(received.substring(4,6),16);
    }

    protected String getCrcFromString(String str,int length) {
        return str.substring(length-4,length);
    }

    protected int concat(int a, int b) {
        String s = String.format("%02d", a) +String.format("%02d", b);
        return Integer.parseInt(s);
    }

    protected String getDataFromString(String str,int length) {
        return str.substring(0,length-3);
    }

    protected String replaceBlank(String withBlank) {
        return withBlank.replaceAll("\\s+", "");
    }

}