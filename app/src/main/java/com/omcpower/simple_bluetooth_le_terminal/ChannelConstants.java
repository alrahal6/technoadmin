package com.omcpower.simple_bluetooth_le_terminal;

public class ChannelConstants {
    static final String COMMON_READINGS = "64 04 00 01 00 0E 29 FB";
    static final String CHANNEL_ONE_READINGS = "64 04 00 13 00 10 09 f6";
    static final String CHANNEL_TWO_READINGS = "64 04 00 23 00 10 09 f9";
    static final String CHANNEL_THREE_READINGS = "64 04 00 33 00 10 08 3c";
    static final String CHANNEL_FOUR_READINGS = "64 04 00 43 00 10 09 e7";
    static final String CHANNEL_FIVE_READINGS = "64  04 00 53 00 10 08 22";

    // MC max current
    static final String CHANNEL_ONE_MC = "64 03 00 ae 00 01 ec 1e";
    static final String CHANNEL_TWO_MC = "64 03 00 af 00 01 bd de";
    static final String CHANNEL_THREE_MC = "64 03 00 b0 00 01 8c 18";
    static final String CHANNEL_FOUR_MC = "64 03 00 b1 00 01 dd d8";
    static final String CHANNEL_FIVE_MC = "64 03 00 b2 00 01 2d d8";

    // balance read and write
    static final String CHANNEL_BALANCE = "64 03 00 68 00 05 0d e0";
    // 01 2c is the hexadecimal for amount 300
    static final String WRITE_BAL_CH01 = "64 06 00 68 01 2c 0a ae";

    // Allowed WH
    static final String ALLOWED_WH = "64 03 00 80 00 05 8d d4";

    // Overload Limit
    static final String OVERLOAD_LIMIT = "64 03 00 74 00 05 cc 26";

    // TOD read
    static final String TOD_READ = "64 03 00 6e 00 06 ad e0";

    // Year  Read
    static final String CHANNEL_ONE_YEAR = "6403008600016C16";
    static final String CHANNEL_TWO_YEAR = "6403008800010DD5";
    static final String CHANNEL_THREE_YEAR = "6403008a0001AC15";
    static final String CHANNEL_FOUR_YEAR = "6403008c00014C14";
    static final String CHANNEL_FIVE_YEAR = "6403008e0001EDD4";
    // Day_Month Read
    static final String CHANNEL_ONE_DAY_MONTH = "6403008700013DD6";
    static final String CHANNEL_TWO_DAY_MONTH = "6403008900015C15";
    static final String CHANNEL_THREE_DAY_MONTH = "6403008b0001FDD5";
    static final String CHANNEL_FOUR_DAY_MONTH = "6403008d00011DD4";
    static final String CHANNEL_FIVE_DAY_MONTH = "6403008f0001BC14";

    /*
    SYSTEM_START_DELAY	0x01
    MONTHLY_RENTAL_FAULT	0x02
    MONTHLY_WH_LIMIT_FAULT 	 0x04
    CURRENT_UNBLCE_FAULT    	0x08
    TOD_INACTIVE_FAULT      	0x10
    OVERLOAD_OCCRD_FAULT    	0x20
    VALID_TILL_DATE_EXPIRED	0x40
    */


}
