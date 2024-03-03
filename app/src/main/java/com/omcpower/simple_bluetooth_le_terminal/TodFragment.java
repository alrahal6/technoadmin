package com.omcpower.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class TodFragment extends BaseFragment implements View.OnClickListener {

    private int channelBalanceId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialStart = false;
        connected = Connected.True;
        //setHasOptionsMenu(true);
        setRetainInstance(true);
        channelBalanceId = getArguments().getInt("selectedChannel");
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
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    private int startHours,startMinutes,endHours,endMinutes,
            startHoursTwo,startMinutesTwo,endHoursTwo,endMinutestwo;

    private TextView st,et,stTwo,etTwo;
    private TextView viewSt,viewEt,viewStTwo,viewEtTwo;
    private TextView todChannel;
    private LinearLayout rHide,rSuccess;


    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duration, container, false);

        rHide = view.findViewById(R.id.duration_hide);
        rSuccess = view.findViewById(R.id.duration_success);

        st = view.findViewById(R.id.start_time);
        et = view.findViewById(R.id.end_time);
        stTwo = view.findViewById(R.id.start_time_two);
        etTwo = view.findViewById(R.id.end_time_two);

        viewSt = view.findViewById(R.id.view_start_one);
        viewSt.setOnClickListener(this);
        viewEt = view.findViewById(R.id.view_end_one);
        viewEt.setOnClickListener(this);
        viewStTwo = view.findViewById(R.id.view_start_two);
        viewStTwo.setOnClickListener(this);
        viewEtTwo = view.findViewById(R.id.view_end_two);
        viewEtTwo.setOnClickListener(this);

        todChannel = view.findViewById(R.id.tod_channel);

        Button setSt = view.findViewById(R.id.set_start_one);
        setSt.setOnClickListener(this);
        Button setEt = view.findViewById(R.id.set_end_one);
        setEt.setOnClickListener(this);
        Button setStTwo = view.findViewById(R.id.set_start_two);
        setStTwo.setOnClickListener(this);
        Button setEtTwo = view.findViewById(R.id.set_end_two);
        setEtTwo.setOnClickListener(this);


        todChannel.setText("TOD - Channel : "+channelBalanceId);
        Button viewTod = view.findViewById(R.id.view_tod);
        /*viewTod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process = Process.TodRead;
                send(ChannelConstants.TOD_READ);
            }
        });*/
        st.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        startHours = timePicker.getHour();
                        startMinutes = timePicker.getMinute();
                        timeStarToWrite = concat(startHours,startMinutes);
                        st.setText("Start Time : "+startHours+":"+startMinutes);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        et.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        endHours = timePicker.getHour();
                        endMinutes = timePicker.getMinute();
                        timeEndToWrite = concat(endHours,endMinutes);
                        et.setText("End Time : "+endHours+":"+endMinutes);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        stTwo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        startHoursTwo = timePicker.getHour();
                        startMinutesTwo = timePicker.getMinute();
                        timeStartTwoToWrite = concat(startHoursTwo,startMinutesTwo);
                        stTwo.setText("Start Time : "+startHoursTwo+":"+startMinutesTwo);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        etTwo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        endHoursTwo = timePicker.getHour();
                        endMinutestwo = timePicker.getMinute();
                        timeEndTwoToWrite = concat(endHoursTwo,endMinutestwo);
                        etTwo.setText("Start Time : "+endHoursTwo+":"+endMinutestwo);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        return view;
    }

    @Override
    void showResponse(String data) {
        resetValues();
        if(process == Process.TodRead) {
            convertResult(data);
        } else if(process == Process.TodWrite) {
            isWriteProcess = false;
            rHide.setVisibility(View.GONE);
            rSuccess.setVisibility(View.VISIBLE);
        }
    }

    private void resetValues() {
        timeStarToWrite = 0;
        timeEndToWrite = 0;
        timeStartTwoToWrite = 0;
        timeEndTwoToWrite = 0;
    }

    private void convertResult(String data) {
        String withoutWhite = data.replaceAll("\\s+", "");
        String startTime;

        startTime = withoutWhite.substring(6, 10);
        int start = Integer.parseInt(startTime, 16);

        switch (todId) {
            case 1:
                st.setText("Start Time : " + start);
            break;
            case 2:
                et.setText("End Time : " + start);
                break;
            case 3:
                stTwo.setText("Start Time : " + start);
                break;
            case 4:
                etTwo.setText("End Time : " + start);
                break;
        }
    }

    private void writeTod(String todHexa,int time) {
        process = Process.TodWrite;
        isWriteProcess = true;
        String runtimeCrc = todHexa + " " + valueAndCrc(todHexa, time);
        send(runtimeCrc);
    }

    private String todStr = "";
    private int timeStarToWrite = 0;
    private int timeEndToWrite = 0;
    private int timeStartTwoToWrite = 0;
    private int timeEndTwoToWrite = 0;

    private void setTodWriteString() {
        switch (channelBalanceId) {
            case 1:
                todWrOne = "64060090";
                todWrTwo = "64060091";
                todWrThree = "64060092";
                todWrFour = "64060093";
                break;
            case 2:
                todWrOne = "64060096";
                todWrTwo = "64060097";
                todWrThree = "64060098";
                todWrFour = "64060099";
                break;
            case 3:
                todWrOne = "6406009C";
                todWrTwo = "6406009D";
                todWrThree = "6406009E";
                todWrFour = "6406009F";
                break;
            case 4:
                todWrOne = "640600A2";
                todWrTwo = "640600A3";
                todWrThree = "640600A4";
                todWrFour = "640600A5";
                break;
            case 5:
                todWrOne = "640600A8";
                todWrTwo = "640600A9";
                todWrThree = "640600AA";
                todWrFour = "640600AB";
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_start_one:
                setTodWriteString();
                if(timeStarToWrite > 0) {
                    writeTod(todWrOne,timeStarToWrite);
                } else {
                    Toast.makeText(getActivity(),"Please set time",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.set_end_one:
                setTodWriteString();
                if(timeEndToWrite > 0) {
                    writeTod(todWrTwo,timeEndToWrite);
                } else {
                    Toast.makeText(getActivity(),"Please set time",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.set_start_two:
                setTodWriteString();
                if(timeStartTwoToWrite > 0) {
                    writeTod(todWrThree,timeStartTwoToWrite);
                } else {
                    Toast.makeText(getActivity(),"Please set time",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.set_end_two:
                setTodWriteString();
                if(timeEndTwoToWrite > 0) {
                    writeTod(todWrFour,timeEndTwoToWrite);
                } else {
                    Toast.makeText(getActivity(),"Please set time",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.view_start_one:
                //todStr = "64 06 00 66";
                todId = 1;
                process = Process.TodRead;
                setChannelTod(channelBalanceId);
                send(todOne);
                break;
            case R.id.view_end_one:
                //todStr = "64 06 00 66";
                todId = 2;
                process = Process.TodRead;
                setChannelTod(channelBalanceId);
                send(todTwo);
                break;
            case R.id.view_start_two:
                //todStr = "64 06 00 66";
                todId = 3;
                process = Process.TodRead;
                setChannelTod(channelBalanceId);
                send(todThree);
                break;
            case R.id.view_end_two:
                //todStr = "64 06 00 66";
                todId = 4;
                process = Process.TodRead;
                setChannelTod(channelBalanceId);
                send(todFour);
                break;
        }
    }

    private int todId = 0;
    private String todOne,todTwo,todThree,todFour;
    private String todWrOne,todWrTwo,todWrThree,todWrFour;

    private void setChannelTod(int channelId) {
        //String tod = "";
        switch (channelId) {
            case 1:
                todOne = "6403009000018DD2";
                todTwo = "640300910001DC12";
                todThree = "6403009200012C12";
                todFour = "6403009300017DD2";
                break;
            case 2:
                todOne = "6403009600016DD3";
                todTwo = "6403009700013C13";
                todThree = "6403009800010C10";
                todFour = "6403009900015DD0";
                break;
            case 3:
                todOne = "6403009C00014DD1";
                todTwo = "6403009D00011C11";
                todThree = "6403009E0001EC11";
                todFour = "6403009F0001BDD1";
                break;
            case 4:
                todOne = "640300A200012C1D";
                todTwo = "640300A300017DDD";
                todThree = "640300A40001CC1C";
                todFour = "640300A500019DDc";
                break;
            case 5:
                todOne = "640300A800010C1F";
                todTwo = "640300A900015DDf";
                todThree = "640300AA0001ADDF";
                todFour = "640300AB0001FC1F";
                break;

        }
    }
}