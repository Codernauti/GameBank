package com.codernauti.gamebank.util;

/**
 * Created by Eduard on 27-Feb-18.
 */

public interface Event {

    interface Game {
        String TRANSACTION = "transaction";
        String START_GAME = "start_game";
        String STOP_GAME = "stop_game";
        String LEADERBOARD = "leaderboard";
        String LIST_TRANSACTIONS = "list_transactions";

        // RoomActivity
        String MEMBER_JOINED = "member_joined";
        String POKE = "poke";
        String MEMBER_READY = "member_ready";
        String MEMBER_DISCONNECT = "member_dsconnect";
    }

    interface Network {
        String CONN_ESTABLISHED = "conn_established";
        String CONN_ERRONEOUS = "conn_erroneous";
        String INIT_INFORMATION = "init_information";
        String SEND_DATA_ERROR = "send_data_error";
    }

    interface Util {
        String SAME_NICKNAME_ERROR = "same_nickname";
        String LOBBY_FULL_ERROR = "lobby_full";
        String MATCH_ABORTED = "match_aborted";
    }

}
