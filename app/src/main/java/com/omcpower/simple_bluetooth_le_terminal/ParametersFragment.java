package com.omcpower.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParametersFragment extends BaseFragment {

    private int channelId = 0;
    private String dashBoardDetails = "";
    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        initialStart = false;
        connected = Connected.True;
        setRetainInstance(true);
        channelId = getArguments().getInt("selectedChannel");
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    private TextView channel;
    private TextView current,load,powerFactor,
            currentMonth,lastMonth,currentBill,lastBill,sysFault;
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parameters, container, false);

        channel = view.findViewById(R.id.channel_no);
        current = view.findViewById(R.id.current);
        load = view.findViewById(R.id.load);
        powerFactor = view.findViewById(R.id.power_factor);
        currentMonth = view.findViewById(R.id.current_month);
        lastMonth = view.findViewById(R.id.last_month);
        currentBill = view.findViewById(R.id.current_bill);
        lastBill = view.findViewById(R.id.last_bill);
        sysFault = view.findViewById(R.id.system_fault);
        channel.setText("Channel-"+channelId);
        Button viewChannel = view.findViewById(R.id.view_channel);

        viewChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //process = Process.DashBoardChannel;
                switch (channelId) {
                    case 1:
                        sendRoute(ChannelConstants.CHANNEL_ONE_READINGS);
                    break;
                    case 2:
                        sendRoute(ChannelConstants.CHANNEL_TWO_READINGS);
                        break;
                    case 3:
                        sendRoute(ChannelConstants.CHANNEL_THREE_READINGS);
                        break;
                    case 4:
                        sendRoute(ChannelConstants.CHANNEL_FOUR_READINGS);
                        break;
                    case 5:
                        sendRoute(ChannelConstants.CHANNEL_FIVE_READINGS);
                        break;
                }
            }
        });

        return view;
    }

    private void channelDetails(int one,int two, int three,int four,
                                int five,int six,int seven,int eight) {
        channel.setText("Channel-"+channelId);
        current.setText("Current : "+(float)one/1000+" A");
        load.setText("Load : "+two+ " W");
        powerFactor.setText("Power Factor : "+(float)three/100);
        currentMonth.setText("Till Date : "+four+" WH");
        lastMonth.setText("Last Month : "+seven+" WH");
        currentBill.setText("Current Bill : "+six+" days");
        lastBill.setText("Last Bill Payment Date : "+dateSeparator(Integer.toString(five))+" ");
        sysFault.setText("System Fault: "+getCodeText(eight));
    }

    private String getCodeText(int value) {
        String text = "";
        int code = value;
        if(code % 2 == 1 ) {
            text += "SYSTEM START DELAY\n";
            code -= 1;
        }
        if(code >= 128) {
            text += "MAX CURRENT FAULT\n";
            code -= 128;
        }
        if(code >= 64) {
            text += "VALID TILL DATE EXPIRED\n";
            code -= 64;
        }
        if(code >= 32) {
            text += "OVERLOAD OCCURRED FAULT\n";
            code -= 32;
        }
        if(code >= 16) {
            text += "TOD INACTIVE FAULT\n";
            code -= 16;
        }
        if(code >= 8) {
            text += "CURRENT UNBALANCE FAULT\n";
            code -= 8;
        }
        if(code >= 4) {
            text += "MONTHLY WH LIMIT FAULT\n";
            code -= 4;
        }
        if(code == 2) {
            text += "MONTHLY RENTAL FAULT\n";
            //code -= 64;
        }
        return text;
    }

    @Override
    void showResponse(String data) {
        convertMe(data,1);
    }

    private void convertMe(String readBuff, int flag) {
        String withoutWhite = readBuff.replaceAll("\\s+", "");
        //Log.d("custom : ", withoutWhite + "");
        String mId = withoutWhite.substring(6, 14);
        String mSno = withoutWhite.substring(14, 22);
        String rDt = withoutWhite.substring(22, 30);
        String rTime = withoutWhite.substring(30, 38);
        String relay = withoutWhite.substring(38, 46);
        String voltage = withoutWhite.substring(46, 54);
        String frequency = withoutWhite.substring(54, 62);
        String sysFault = withoutWhite.substring(62, 70);
        int one = Integer.parseInt(mId, 16);
        int two = Integer.parseInt(mSno, 16);
        int three = Integer.parseInt(rDt, 16);
        int four = Integer.parseInt(rTime, 16);
        int five = Integer.parseInt(relay, 16);
        int six = Integer.parseInt(voltage, 16);
        int seven = Integer.parseInt(frequency, 16);
        int eight = Integer.parseInt(sysFault, 16);
        channelDetails(one,two,three,four,five,six,seven,eight);
    }
}