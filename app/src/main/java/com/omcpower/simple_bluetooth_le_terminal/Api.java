package com.omcpower.simple_bluetooth_le_terminal;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {

    //String BASE_URL = "http://20.44.52.228/api/";
    String BASE_URL = "http://192.168.29.194:8080/api/";
    //String BASE_URL = "http://192.168.80.166:8080/api/";

    @POST("user.php")
    Call<Results> getValidUsers(@Body Results results);

    @POST("login.php")
    Call<Results> getLoginUsers(@Body Results results);

    @POST("commands.php")
    Call<Command> saveCommand(@Body Command post);
}
