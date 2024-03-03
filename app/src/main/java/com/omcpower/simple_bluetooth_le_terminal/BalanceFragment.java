package com.omcpower.simple_bluetooth_le_terminal;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BalanceFragment extends BaseFragment
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private int channelBalanceId = 1;

    private int currentChannel = 0;

    private TextView tvDate;
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
    private int selectedValue = 0;

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recharge, container, false);
        channelBlock = view.findViewById(R.id.select_channel);
        rechargeBlock = view.findViewById(R.id.recharge_block);
        rHide = view.findViewById(R.id.recharge_hide);
        rSuccess = view.findViewById(R.id.recharge_success);
        rFailure = view.findViewById(R.id.recharge_failed);
        //final EditText userInput = view.findViewById(R.id.recharge_amount);
        tvDate = view.findViewById(R.id.recharge_date);
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Please note that use your package name here
                com.omcpower.simple_bluetooth_le_terminal.DatePicker pd = new com.omcpower.simple_bluetooth_le_terminal.DatePicker();
                pd.setListener(BalanceFragment.this);
                pd.show(getFragmentManager(), "MonthYearPickerDialog");
            }
        });
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
        channelTitle.setText("Channel-"+currentChannel);
        final Button recharge = view.findViewById(R.id.recharge);
        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLength = 0;
                process = Process.Recharge;
                setChannelHexa();
                if(selectedValue == 0) {
                    Toast.makeText(getActivity(),"Please select year",Toast.LENGTH_LONG).show();
                } else {
                    rechargeBalance(selectedValue);
                    tvDate.setText("");
                }
                //String channel = spinner.getSelectedItem().toString();
                //String amt = userInput.getText().toString();
                //rechargeBalance(Integer.parseInt(amt));
                //userInput.setText("");
            }
        });
        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        //Calendar mCalendar = Calendar.getInstance();
        //mCalendar.set(Calendar.YEAR, year);
        //mCalendar.set(Calendar.MONTH, month);
        //mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());
        selectedValue = concat(month,year);
        tvDate.setText(month+""+year);
    }

    private void readBalance(String data) {
        String withoutWhite = data.replaceAll("\\s+", "");
        String balOne;
        balOne = withoutWhite.substring(6, 10);
        //Log.d("custom : ", withoutWhite + " - without white");
        /*switch (currentChannel) {
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
        }*/
        currentBalance = Integer.parseInt(balOne, 16);
        currBal.setText("Day Month Channel-"+channelBalanceId+" :"+currentBalance);
    }

    private void rechargeBalance(int amount) {
        process = Process.Recharge;
        String runtimeCrc = channelhexa+" "+valueAndCrc(channelhexa,amount);
        isWriteProcess = true;
        send(runtimeCrc);
    }

    int currentLength = 0;

    @Override
    void showResponse(String data) {
        if(process == Process.Balance) {
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
        process = Process.Balance;
        switch (currentChannel) {
            case 1:
                channelBalanceId = 1;
                send(ChannelConstants.CHANNEL_ONE_DAY_MONTH);
                channelhexa = "64 06 00 68";
                break;
            case 2:
                channelBalanceId = 2;
                send(ChannelConstants.CHANNEL_TWO_DAY_MONTH);
                channelhexa = "64 06 00 69";
                break;
            case 3:
                channelBalanceId = 3;
                send(ChannelConstants.CHANNEL_THREE_DAY_MONTH);
                channelhexa = "64 06 00 6A";
                break;
            case 4:
                channelBalanceId = 4;
                send(ChannelConstants.CHANNEL_FOUR_DAY_MONTH);
                channelhexa = "64 06 00 6B";
                break;
            case 5:
                channelBalanceId = 5;
                send(ChannelConstants.CHANNEL_FIVE_DAY_MONTH);
                channelhexa = "64 06 00 6C";
                break;
            case R.id.cancel_recharge:
                toggleChannelList();
                break;
        }
    }

    private void toReadBalance() {
        switch (currentChannel) {
            case 1:
                channelBalanceId = 1;
                send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 68";
                break;
            case 2:
                channelBalanceId = 2;
                send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 69";
                break;
            case 3:
                channelBalanceId = 3;
                send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 6A";
                break;
            case 4:
                channelBalanceId = 4;
                send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 6B";
                break;
            case 5:
                channelBalanceId = 5;
                send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 6C";
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
                channelhexa = "64 06 00 87";
                break;
            case 2:
                //channelBalanceId = 2;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 89";
                break;
            case 3:
                //channelBalanceId = 3;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 8B";
                break;
            case 4:
                //channelBalanceId = 4;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 8D";
                break;
            case 5:
                //channelBalanceId = 5;
                //send(ChannelConstants.CHANNEL_BALANCE);
                channelhexa = "64 06 00 8F";
                break;
            case R.id.cancel_recharge:
                toggleChannelList();
                break;
        }
    }

    String channelhexa = "";

}