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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChannelSelectionFragment extends BaseFragment {

    /*
     * Lifecycle
     */
    private int selectedChannel = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialStart = false;
        connected = Connected.True;
        //setHasOptionsMenu(true);
        setRetainInstance(true);
        //deviceAddress = getArguments().getString("device");
        selectedChannel = getArguments().getInt("selectedChannel");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations()) {
            service.detach();
        }
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }


    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_selection, container, false);
        TextView channelId = view.findViewById(R.id.selected_channel_id);
        channelId.setText("Channel : "+selectedChannel);
        Button parameter = view.findViewById(R.id.parameter);
        parameter.setOnClickListener(v -> gotoParameter());
        Button balance = view.findViewById(R.id.balance);
        balance.setOnClickListener(v -> gotoBalance());
        Button balanceYear = view.findViewById(R.id.balance_year);
        balanceYear.setOnClickListener(v -> gotoBalanceYear());
        Button overload = view.findViewById(R.id.overload_limit);
        overload.setOnClickListener(v -> gotoOverload());
        Button allowedWh = view.findViewById(R.id.allowed_wh);
        allowedWh.setOnClickListener(v -> gotoAllowedWh());
        Button tod = view.findViewById(R.id.tod);
        tod.setOnClickListener(v -> gotoTod());
        Button setMaxCurrent = view.findViewById(R.id.set_max_current);
        setMaxCurrent.setOnClickListener(v -> setMaxCurrent());
        // Button tod = view.findViewById(R.id.tod);
        // tod.setOnClickListener(v -> gotoTod());
        return view;
    }

    protected void gotoParameter() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new ParametersFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "dashboard").addToBackStack(null).commit();
    }

    protected void gotoBalance() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new BalanceFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoBalanceYear() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new BalanceYearFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoOverload() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new OverloadLimitFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "recharge").addToBackStack(null).commit();
    }

    protected void gotoAllowedWh() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new AllowedWhFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "dashboard").addToBackStack(null).commit();
    }

    protected void gotoTod() {
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new TodFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "tod").addToBackStack(null).commit();
    }

    private void setMaxCurrent() {
        /*
        Bundle args = new Bundle();
        args.putInt("selectedChannel", selectedChannel);
        Fragment fragment = new MaxCurrentFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "tod").addToBackStack(null).commit();
        */
        isWriteProcess = true;
        switch (selectedChannel) {
            case 1:
                send("640600ae1f40e81e");
                break;
            case 2:
                send("640600af1F40b9de");
                break;
            case 3:
                send("640600b01F408818");
                break;
            case 4:
                send("640600b11F40d9d8");
                break;
            case 5:
                send("640600b21F4029d8");
                break;
        }
    }

    @Override
    void showResponse(String data) {
        Toast.makeText(getActivity(),"Command Success",Toast.LENGTH_LONG).show();
    }
}