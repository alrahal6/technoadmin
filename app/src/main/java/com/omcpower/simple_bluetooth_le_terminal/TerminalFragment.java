package com.omcpower.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TerminalFragment extends BaseFragment {

    //protected boolean initialStart = true;
    /*
     * Lifecycle
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connected = Connected.False;
        //setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
        deviceName = getArguments().getString("deviceName");
        userType = getArguments().getInt("userType");
        //getActivity().getActionBar().setTitle(deviceName);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("connecting - "+deviceName);
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False) {
            disconnect();
        }
        actionBar.setTitle("OMC");
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
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
        if(service != null && !getActivity().isChangingConfigurations()) {
            service.detach();
        }
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
        if (connected == Connected.True) {
            showHome();
        }
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
        //et = view.findViewById(R.id.custom_entry);
        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        mConnecting = view.findViewById(R.id.connecting_progress);
        mHome = view.findViewById(R.id.home);
        resetGroup = view.findViewById(R.id.resetGroup);
        //resetGroup.setVisibility(View.VISIBLE);
        //LinearLayout allGroup = view.findViewById(R.id.home);
        if(userType == 1) {
            Button dashBoard = view.findViewById(R.id.goto_dashboard);
            dashBoard.setOnClickListener(v -> gotoDashboard());
            Button cOne = view.findViewById(R.id.goto_channel_one);
            cOne.setOnClickListener(v -> gotoChannelSelection(1));
            Button cTwo = view.findViewById(R.id.goto_channel_two);
            cTwo.setOnClickListener(v -> gotoChannelSelection(2));
            Button cThree = view.findViewById(R.id.goto_channel_three);
            cThree.setOnClickListener(v -> gotoChannelSelection(3));
            Button cFour = view.findViewById(R.id.goto_channel_four);
            cFour.setOnClickListener(v -> gotoChannelSelection(4));
            Button cFive = view.findViewById(R.id.goto_channel_five);
            cFive.setOnClickListener(v -> gotoChannelSelection(5));
            Button monthTariff = view.findViewById(R.id.goto_monthly_tariff);
            monthTariff.setOnClickListener(v -> gotoMonthlyTariff());
            Button delayTime = view.findViewById(R.id.goto_overload_dtime);
            delayTime.setOnClickListener(v -> gotoOverloadDelayTime());
            Button inrushCommand = view.findViewById(R.id.adjust_inrush);
            inrushCommand.setOnClickListener(v -> inrushCommand());
        }
        //Button tod = view.findViewById(R.id.goto_tod);
        //tod.setOnClickListener(v -> gotoTod());
        Button reset = view.findViewById(R.id.reset_all);
        reset.setOnClickListener(v -> resetAll());

        Button rtc = view.findViewById(R.id.rtc);
        rtc.setOnClickListener(v -> setRtc());

        Button resetNew = view.findViewById(R.id.new_command);
        resetNew.setOnClickListener(v -> newCommand());
        return view;
    }

    protected void gotoDashboard() {
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new MaterDataFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "dashboard").addToBackStack(null).commit();
    }

    protected void gotoChannelSelection(int channelId) {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", channelId);
        Fragment fragment = new ChannelSelectionFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoMonthlyTariff() {
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new MonthlyTariffFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoOverloadDelayTime() {
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new OverloadDTimeFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoTod() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", 1);
        Fragment fragment = new TodFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    private void resetAll() {
        //process = Process.Reset;
        //isWriteProcess = false;
        isWriteProcess = true;
        send("6406006F0000B022");
        //Toast.makeText(getActivity(),"Command : 6406006F0000B022",Toast.LENGTH_LONG).show();
        //send("64060073002871FA");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setRtc() {
        isWriteProcess = true;
        //send("6406006F0000B022");
        String hexa = getDateTimeHexa();
        send(hexa+""+valueAndCrc(getDateTimeHexa()));
        //Toast.makeText(getActivity(),hexa+""+valueAndCrc(getDateTimeHexa()),Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDateTimeHexa() {
        Date date = new Date();
        LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        //int year = localDate.getYear();
        String year = Integer.toHexString(0x100 | localDate.getYear() - 2000).substring(1).toUpperCase();
        String month = Integer.toHexString(0x100 | localDate.getMonthValue()).substring(1).toUpperCase();
        String day = Integer.toHexString(0x100 | localDate.getDayOfMonth()).substring(1).toUpperCase();
        String hour = Integer.toHexString(0x100 | localDate.getHour()).substring(1).toUpperCase();
        String min = Integer.toHexString(0x100 | localDate.getMinute()).substring(1).toUpperCase();
        String sec = Integer.toHexString(0x100 | localDate.getSecond()).substring(1).toUpperCase();


        /*int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        int hour = localDate.getHour();
        int min = localDate.getMinute();
        int sec = localDate.getSecond();*/
        //Toast.makeText(getActivity(),year +""+month+""+day+""+hour+""+min+""+sec,Toast.LENGTH_LONG).show();
        return "6406343A"+year +""+month+""+day+""+hour+""+min+""+sec;
    }

    private void inrushCommand() {
        //Toast.makeText(getActivity(),"Command : Inrush",Toast.LENGTH_LONG).show();
        //isWriteProcess = true;
        //send("6406007302eef0c8");
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new UnBalanceFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    private void newCommand() {
        isWriteProcess = true;
        send("64060073002871FA");
        //Toast.makeText(getActivity(),"Command : 64060073002871FA",Toast.LENGTH_LONG).show();
    }

    @Override
    void showResponse(String data) {
        if(process == Process.Reset) {
            send("64060073002871FA");
        }
        Toast.makeText(getActivity(),"Command Success",Toast.LENGTH_LONG).show();

    }
}
