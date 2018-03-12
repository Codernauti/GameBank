package com.codernauti.gamebank;

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

}
