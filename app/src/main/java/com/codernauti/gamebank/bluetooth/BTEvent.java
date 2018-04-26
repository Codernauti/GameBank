package com.codernauti.gamebank.bluetooth;

/**
 * Created by Eduard on 12-Mar-18.
 *
 * Events emitted explicitly by BTConnection
 */

public interface BTEvent {
    String CONN_ESTABLISHED = "conn_established";
    String CONN_ERRONEOUS = "conn_erroneous";
    String SEND_DATA_ERROR = "send_data_error";
    String START = "start";
    String STOP = "stop";
    String MEMBER_CONNECTED = "member_connected";
    String MEMBER_DISCONNECTED = "member_disconnected";
    String CURRENT_STATE = "current_state";
    String HOST_DISCONNECTED = "host_disconnected";
    String MAX_CLIENT_CONN_REACHED = "max_client_conn_reached";
}
