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
        String POKE = "poke";
        String MEMBER_JOINED = "member_joined";
    }

    interface Network {
        String CONN_ESTABLISHED = "conn_established";
        String CONN_ERRONEOUS = "conn_erroneous";
        String INIT_INFORMATION = "init_information";
    }

    interface Util {
        String SAME_NICKNAME_ERROR = "same_nickname";
        String LOBBY_FULL_ERROR = "lobby_full";
        String MATCH_ABORTED = "match_aborted";
    }

}
