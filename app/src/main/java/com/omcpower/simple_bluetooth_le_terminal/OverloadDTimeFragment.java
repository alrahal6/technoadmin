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

public class OverloadDTimeFragment extends BaseFragment {
    private int channelBalanceId = 1;
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
        deviceAddress = getArguments().getString("device");
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

    private void rechargeBalance(int amount) {
        isWriteProcess = true;
        process = Process.MonthlyTariffRecharge;
        String hex = "640600bc";
        String runtimeCrc = hex+""+valueAndCrc(hex,amount);
        //Toast.makeText(getActivity(), "gen: "+runtimeCrc, Toast.LENGTH_LONG).show();
        send(runtimeCrc);
    }

    private TextView currBal;
    private LinearLayout successRecharge,tariffMain;
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overload_d_time, container, false);

        final EditText userInput = view.findViewById(R.id.recharge_amount);
        userInput.setText("5");
        userInput.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "15")});
        tariffMain = view.findViewById(R.id.tariff_main);
        successRecharge = view.findViewById(R.id.tariff_recharge_success);
        currBal = view.findViewById(R.id.current_balance);
        final Button viewBalance = view.findViewById(R.id.view_tariff_details);

        final Button recharge = view.findViewById(R.id.tariff_recharge);
        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLength = 0;

                isWriteProcess = true;
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

        viewBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process = Process.MonthlyTariffBalance;
                send("640300bc00014c1b");
            }
        });
        return view;
    }

    private void readBalance(String data) {
        String withoutWhite = data.replaceAll("\\s+", "");
        // Integer.parseInt(withoutWhite.substring(6, 10), 16)
        currBal.setText("Current O C delay time :" + Integer.parseInt(withoutWhite.substring(6, 10), 16));
    }

    int currentLength = 0;

    @Override
    void showResponse(String data) {
        if(process == Process.MonthlyTariffBalance) {
            readBalance(data);
        } else if(process == Process.MonthlyTariffRecharge) {
            //readBalance(data);
            isWriteProcess = false;
            tariffMain.setVisibility(View.GONE);
            successRecharge.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), "Recharge Successful ", Toast.LENGTH_LONG).show();

        }
    }

}