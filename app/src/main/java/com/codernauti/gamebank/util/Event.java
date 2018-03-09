package com.codernauti.gamebank.util;

/**
 * Created by Eduard on 27-Feb-18.
 */

public interface Event {

    // Event emitted explicitly by Game part
    interface Game {

        // Bank
        String TRANSACTION = "transaction";
        String LIST_TRANSACTIONS = "list_transactions";
        String LEADERBOARD = "leaderboard";

        // Lobby
        String POKE = "poke";
        String MEMBER_READY = "member_ready";
    }

    // Event emitted explicitly by BTConnection
    interface Network {
        String CONN_ESTABLISHED = "conn_established";
        String CONN_ERRONEOUS = "conn_erroneous";
        String SEND_DATA_ERROR = "send_data_error";
        String START = "start";
        String STOP = "stop";
        String MEMBER_CONNECTED = "member_connected";
        String MEMBER_DISCONNECTED = "member_disconnected";
        String CURRENT_STATE = "current_state";
        String HOST_DISCONNECTED = "host_disconnected";
    }

}
