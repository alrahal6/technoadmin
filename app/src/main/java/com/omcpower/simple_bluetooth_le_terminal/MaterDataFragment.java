package com.omcpower.simple_bluetooth_le_terminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MaterDataFragment extends BaseFragment {

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialStart = false;
        connected = Connected.True;
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    private TextView deviceId,sNo,dateMd,timeMd,relay,voltage,frequency;
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        TextView masterData = view.findViewById(R.id.master_data);
        Button viewMaster = view.findViewById(R.id.view_md_details);

        viewMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process = Process.Dashboard;
                sendRoute(ChannelConstants.COMMON_READINGS);
            }
        });
        deviceId = view.findViewById(R.id.device_id);
        sNo = view.findViewById(R.id.serial_no);
        dateMd = view.findViewById(R.id.date_md);
        timeMd = view.findViewById(R.id.time_md);
        relay = view.findViewById(R.id.relay);
        voltage = view.findViewById(R.id.voltage);
        frequency = view.findViewById(R.id.frequency);

        return view;
    }

    private void dashboardDetails(int one, int two, int three, int four,
                                  int five, int six, int seven) {
        deviceId.setText("Device Id : "+one);
        sNo.setText("Serial No. : "+two);
        dateMd.setText("Date : "+dateSeparator(Integer.toString(three)));
        timeMd.setText("Time : "+timeSeparator(Integer.toString(four)));
        relay.setText("Relay : "+relayText(five));
        voltage.setText("Voltage : "+(float)six/100+" V");
        frequency.setText("Frequency : "+(float)seven/10+" Hz");
    }


    private String relayText(int value) {
        String text = "";
        switch (value) {
            case 31:
                text = "all on";
                break;
            case 30:
                text = "first rely off";
                break;
            case 29:
                text = "second off";
                break;
            case 28:
                text = "1+2 off";
                break;
            case 27:
                text = "3 off";
                break;
            case 26:
                text = "1+3 off";
                break;
            case 25:
                text = "2+3 off";
                break;
            case 24:
                text = "1+2+3 off";
                break;
            case 23:
                text = "4 off";
                break;
            case 22:
                text = "1+4 off";
                break;
            case 21:
                text = "2+4 off";
                break;
            case 20:
                text = "1+2+4 off";
                break;
            case 19:
                text = "3+4  off";
                break;
            case 18:
                text = "1+3+4 off";
                break;
            case 17:
                text = "2+3+4 off";
                break;
            case 16:
                text = "1+2+3+4 off";
                break;
            case 15:
                text = "5 off";
                break;
            case 14:
                text = "1+5 off";
                break;
            case 13:
                text = "2+5 off";
                break;
            case 12:
                text = "1+2+5 off";
                break;
            case 11:
                text = "3+5 off";
                break;
            case 10:
                text = "1+3+5 off";
                break;
            case 9:
                text = "2+3+5 off";
                break;
            case 8:
                text = "1+2+3+5 off";
                break;
            case 7:
                text = "4+5 off";
                break;
            case 6:
                text = "1+4+5 off";
                break;
            case 5:
                text = "2+4+5";
                break;
            case 4:
                text = "1+2+4+5 off";
                break;
            case 3:
                text = "3+4+5 off";
                break;
            case 2:
                text = "1+3+4+5 off";
                break;
            case 1:
                text = "2+3+4+5 off";
                break;
            case 0:
                text = "1+2+3+4+5  off";
                break;

        }
        return text;
    }


    @Override
    void showResponse(String data) {
        convertMe(data);
    }

    private void convertMe(String readBuff) {
        String withoutWhite = readBuff.replaceAll("\\s+", "");
        //Log.d("custom : ", withoutWhite + "");
        String mId = withoutWhite.substring(6, 14);
        String mSno = withoutWhite.substring(14, 22);
        String rDt = withoutWhite.substring(22, 30);
        String rTime = withoutWhite.substring(30, 38);
        String relay = withoutWhite.substring(38, 46);
        String voltage = withoutWhite.substring(46, 54);
        String frequency = withoutWhite.substring(54, 62);
        int one = Integer.parseInt(mId, 16);
        int two = Integer.parseInt(mSno, 16);
        int three = Integer.parseInt(rDt, 16);
        int four = Integer.parseInt(rTime, 16);
        int five = Integer.parseInt(relay, 16);
        int six = Integer.parseInt(voltage, 16);
        int seven = Integer.parseInt(frequency, 16);
        dashboardDetails(one,two,three,four,five,six,seven);
    }
}