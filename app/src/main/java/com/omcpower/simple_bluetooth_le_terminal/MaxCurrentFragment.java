package com.omcpower.simple_bluetooth_le_terminal;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MaxCurrentFragment extends BaseFragment implements View.OnClickListener {

    private int channelBalanceId = 1;

    private int currentChannel = 0;
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
        currentChannel = getArguments().getInt("selectedChannel");
        channelBalanceId = currentChannel;
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

    private int currentBalance = 0;

    private TextView currBal;
    private LinearLayout channelBlock,rechargeBlock,rHide,rSuccess,rFailure;
    private TextView channelTitle;
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_max_current, container, false);
        channelBlock = view.findViewById(R.id.select_channel);
        rechargeBlock = view.findViewById(R.id.recharge_block);
        rHide = view.findViewById(R.id.recharge_hide);
        rSuccess = view.findViewById(R.id.recharge_success);
        rFailure = view.findViewById(R.id.recharge_failed);
        final EditText userInput = view.findViewById(R.id.recharge_amount);
        //userInput.setText("5");
        userInput.setFilters(new InputFilter[]{new InputFilterMinMax("1", "4000")});
        final TextView channelOne = view.findViewById(R.id.channel_one);
        channelOne.setOnClickListener(this);
        final TextView channelTwo = view.findViewById(R.id.channel_two);
        channelTwo.setOnClickListener(this);
        final TextView channelThree = view.findViewById(R.id.channel_three);
        channelThree.setOnClickListener(this);
        final TextView channelFour = view.findViewById(R.id.channel_four);
        channelFour.setOnClickListener(this);
        final TextView channelFive = view.findViewById(R.id.channel_five);
        channelFive.setOnClickListener(this);
        Button cancelRecharge = view.findViewById(R.id.cancel_recharge);
        cancelRecharge.setOnClickListener(this);
        Button viewBalance = view.findViewById(R.id.view_balance);
        viewBalance.setOnClickListener(this);
        channelTitle = view.findViewById(R.id.recharge_title);
        currBal = view.findViewById(R.id.current_balance);
        //final Button viewBalance = view.findViewById(R.id.view_recharge_details);
        channelTitle.setText("Max Current Channel-"+currentChannel);
        final Button recharge = view.findViewById(R.id.recharge);
        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLength = 0;
                process = Process.Recharge;
                setChannelHexa();
                //String channel = spinner.getSelectedItem().toString();
                String amt = userInput.getText().toString().trim();
                if(amt.matches("")) {
                    Toast.makeText(getActivity(), "Please enter value ", Toast.LENGTH_LONG).show();
                } else {
                    rechargeBalance(Integer.parseInt(amt));
                    userInput.setText("");
                }
            }
        });
        return view;
    }

    private void readBalance(String data) {
        String withoutWhite = data.replaceAll("\\s+", "");
        String balOne;
        //Log.d("custom : ", withoutWhite + " - without white");
        switch (currentChannel) {
            case 1:
                balOne = withoutWhite.substring(6, 10);
                break;
            case 2:
                balOne = withoutWhite.substring(10, 14);
                break;
            case 3:
                balOne = withoutWhite.substring(14, 18);
                break;
            case 4:
                balOne = withoutWhite.substring(18, 22);
                break;
            case 5:
                balOne = withoutWhite.substring(22, 26);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + channelBalanceId);
        }
        currentBalance = Integer.parseInt(balOne, 16);
        currBal.setText("Max Current :"+currentBalance+" Wh");
    }

    private void rechargeBalance(int amount) {
        process = Process.Recharge;
        isWriteProcess = true;
        String runtimeCrc = channelhexa+" "+valueAndCrc(channelhexa,amount);
        //Toast.makeText(getActivity(),"str: "+ runtimeCrc,Toast.LENGTH_LONG).show();
        send(runtimeCrc);
    }

    int currentLength = 0;

    @Override
    void showResponse(String data) {
        if(process == Process.MaxCurrent) {
            readBalance(data);
        } else if(process == Process.Recharge) {
            isWriteProcess = false;
            rHide.setVisibility(View.GONE);
            rSuccess.setVisibility(View.VISIBLE);
        }
    }

    private boolean isOnChannel = true;
    private void toggleChannelList() {
        if(isOnChannel) {
            channelBlock.setVisibility(View.GONE);
            rechargeBlock.setVisibility(View.VISIBLE);
            isOnChannel = false;
        } else {
            channelBlock.setVisibility(View.VISIBLE);
            rechargeBlock.setVisibility(View.GONE);
            isOnChannel = true;
        }
    }

    @Override
    public void onClick(View view) {
        process = Process.MaxCurrent;
        switch (currentChannel) {
            case 1:
                channelBalanceId = 1;
                send(ChannelConstants.CHANNEL_ONE_MC);
                //channelhexa = "64 06 00 80";
                break;
            case 2:
                channelBalanceId = 2;
                send(ChannelConstants.CHANNEL_TWO_MC);
                //channelhexa = "64 06 00 81";
                break;
            case 3:
                channelBalanceId = 3;
                send(ChannelConstants.CHANNEL_THREE_MC);
                //channelhexa = "64 06 00 82";
                break;
            case 4:
                channelBalanceId = 4;
                send(ChannelConstants.CHANNEL_FOUR_MC);
                //channelhexa = "64 06 00 83";
                break;
            case 5:
                channelBalanceId = 5;
                send(ChannelConstants.CHANNEL_FIVE_MC);
                //channelhexa = "64 06 00 84";
                break;
            case R.id.cancel_recharge:
                toggleChannelList();
                break;
        }
    }

    private void setChannelHexa() {
        switch (currentChannel) {
            case 1:
                //channelBalanceId = 1;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 ae";
                break;
            case 2:
                //channelBalanceId = 2;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 af";
                break;
            case 3:
                //channelBalanceId = 3;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 b0";
                break;
            case 4:
                //channelBalanceId = 4;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 b1";
                break;
            case 5:
                //channelBalanceId = 5;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 b2";
                break;
            case R.id.cancel_recharge:
                toggleChannelList();
                break;
        }
    }
    String channelhexa = "";

}