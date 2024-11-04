package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;; //env.config.turnTimeoutMillis;//Long.MAX_VALUE;

    public Queue<Integer> firstPlayerTo3;

    public Thread dealerThread;

    private boolean reset;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        firstPlayerTo3 = new LinkedList<Integer>();
        reset = false;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        dealerThread = Thread.currentThread();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
        for (Player player : players) {
            player.playerThread = new Thread(player);
            player.playerThread.start();
        }
        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(true);
            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        updateTimerDisplay(true);
        while (!terminate && System.currentTimeMillis() < reshuffleTime && table.countCards()>0) {
            sleepUntilWokenOrTimeout(); //player notify dealer to check if there is a set
            updateTimerDisplay(reset);
            reset = false;
            removeCardsFromTable(); //cards are removed if the dealer founds set
            placeCardsOnTable(); //new cards are placed
        }

    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        // TODO implement
        synchronized(this){
            this.notifyAll();
        }
        for(Player p: players)
        {
            p.terminate();

            /*synchronized(p){
                p.notifyAll();
            }*/
        }
        terminate = true;
        
        env.ui.dispose();
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement
        while(!firstPlayerTo3.isEmpty())
        {
            int firstId = firstPlayerTo3.remove();
            boolean isSet = false;
            for(int i=0; i < players.length; i++)
            {
                if(players[i].id == firstId)
                {
                    synchronized(players[i]){
                        if(players[i].actions.size() == 3){
                            int [] actionsArray = players[i].actions.stream().mapToInt(j -> table.slotToCard[j]).toArray();
                            isSet = env.util.testSet(actionsArray);
                            if(isSet)
                            {
                                for(int k=0; k<players[i].actions.size(); k++){
                                    table.removeToken(i, players[i].actions.get(k));
                                    for (Player p :players) {
                                        try{
                                            if(p.id!=players[i].id){
                                                table.removeToken(p.id, players[i].actions.get(k));
                                                p.actions.remove(p.actions.indexOf(players[i].actions.get(k)));
                                            }
                                        }catch(Exception e){}
                                    }
                                    table.removeCard(players[i].actions.get(k));
                                }
                                players[i].actions.clear();
                                reset = true;
                            }
                        } 
                        players[i].notifyAll();
                        if(isSet){
                            players[i].point = true;
                        }
                        else{
                            players[i].penalty = true;
                        }
                    }
                }
            }
        }
        
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement
        synchronized(table){
            Random rand = new Random();
            for(int i=0; i<table.slotToCard.length; i++)
            {
                if(table.slotToCard[i] ==null && deck.size()>0){
                    int index = rand.nextInt(deck.size());
                    table.placeCard(deck.get(index), i);
                    deck.remove(index);
                }
            }
            table.notifyAll();
        }
    }

    public void placeCardsOnTableForTest(){
        placeCardsOnTable();
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        synchronized(dealerThread){
            try {
                int time = 0;
                while(time < 60999 && !terminate){
                    updateTimerDisplay(reset);
                    dealerThread.sleep(1000);
                    time+=1000;
                    reset = false;
                }
            }
            catch(InterruptedException e)
            {
                dealerThread.interrupt();
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement

        if(reset){
            this.reshuffleTime = System.currentTimeMillis() + 60999;
            env.ui.setCountdown(60999,false);
        }
        else{
            if(reshuffleTime-System.currentTimeMillis()<=10000)
                env.ui.setCountdown(reshuffleTime-System.currentTimeMillis(), true);
            else
                env.ui.setCountdown(reshuffleTime-System.currentTimeMillis(), false);
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        synchronized(table){
            env.ui.removeTokens();
            for(Player p: players)
                p.actions.clear();
            for(int i=0; i < table.slotToCard.length; i++)
            {
                if(table.slotToCard[i] != null)
                {
                    deck.add(table.slotToCard[i]);
                    table.removeCard(i);
                }
            }
        }
    }

    public void removeAllCardsFromTableForTest(){
        removeAllCardsFromTable();
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        try{
            Thread.sleep(env.config.pointFreezeMillis);
        }catch(InterruptedException e){}
        int maxScore = -1;
        Vector<Integer> winnersIds = new Vector<>();
        for (Player p : players) {
            if(p.score() > maxScore)
                maxScore = p.score();
        }
        for (Player p : players) {
            if(p.score() == maxScore)
                winnersIds.add(p.id);
        }
        int index = 0;
        int [] winnersIdsArray = new int[winnersIds.size()];
        for (int j : winnersIds) {
            winnersIdsArray[index] = j;
            index++;
        }
        env.ui.announceWinner(winnersIdsArray);
    }

    public void announceWinnersForTests(){
        announceWinners();
    }

    public int countDeck()
    {
        return deck.size();
    }
}
