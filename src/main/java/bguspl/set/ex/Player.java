package bguspl.set.ex;

import java.sql.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    public Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    public LinkedList<Integer> actions;

    private Dealer dealer;

    public boolean three_slots;

    public boolean point;

    public boolean penalty;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.dealer = dealer;
        this.actions = new LinkedList<>();
        this.three_slots = false;
        this.point = false;
        this.penalty = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + "starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop
            synchronized(table){
                try{
                    while(table.countCards() ==0){
                        table.wait();
                    }
                }catch(InterruptedException e){}
            }
            synchronized(actions)
            {
                try{
                    while(actions.isEmpty())
                        actions.wait();
                }
                catch(InterruptedException e){}
            }
            if(three_slots){
                synchronized(this){
                    try{
                        dealer.firstPlayerTo3.add(id);
                        dealer.dealerThread.interrupt();
                        this.wait();
                        if(point)
                            point();     
                        else if(penalty)
                            penalty();
                        
                    }catch(InterruptedException e){}
                }
                three_slots = false;
            }    
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
            Integer [] notNullcards;
            while (!terminate) {
                // TODO implement player key press simulator
                synchronized(table){
                    try{
                        while(table.countCards() ==0){
                            table.wait();
                        }
                    }catch(InterruptedException e){}
                }
                notNullcards = Arrays.stream(table.cardToSlot).filter(Objects::nonNull).toArray(Integer[]::new);
                System.out.println(notNullcards.length);
                Random rand = new Random();
                int cardIndex = rand.nextInt(notNullcards.length);
                keyPressed(notNullcards[cardIndex]);
                notNullcards = null;
                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){}
    
            }
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        terminate = true;
        /* 
        try{
            playerThread.join();
        }catch(InterruptedException e){}
        */
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement
        if(!(table.countCards()<12 && dealer.countDeck()>0) && (!point) && (!penalty)){
            synchronized(actions){
                actions.notifyAll();
                if(actions.size() < 3) {
                    if(!actions.contains(slot)) {
                        if(table.slotToCard[slot]!=null){
                            actions.add(slot);
                            table.placeToken(id,slot);
                            if(actions.size() == 3)
                            {
                                three_slots = true;
                            }
                        }
                    }
                    else {
                        actions.remove(actions.indexOf(slot));
                        table.removeToken(id,slot);
                    }
                }
                else if(actions.size() == 3){
                    
                    if(actions.contains(slot)) {
                        actions.remove(actions.indexOf(slot));
                        table.removeToken(id, slot);
                    }
                }
            }
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement

        try {
            long freezeTime = env.config.pointFreezeMillis - 1000;
            //long timer = System.currentTimeMillis() + freezeTime; 
            while(freezeTime >= 0){
                env.ui.setFreeze(id, freezeTime + 1000);
                Thread.sleep(1000);
                freezeTime = freezeTime - 1000;
                if(freezeTime == -1000)
                    env.ui.setFreeze(id, 0);
            }

            point = false;
        } catch (InterruptedException ignored) {}

        //int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        env.ui.setScore(id, ++score);
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
       
        try {
            long freezeTime = env.config.penaltyFreezeMillis - 1000;
            //long timer = System.currentTimeMillis() + freezeTime; 
            
            while(freezeTime >= 0){
                env.ui.setFreeze(id, freezeTime + 1000);
                Thread.sleep(1000);
                freezeTime = freezeTime - 1000;
                if(freezeTime == -1000)
                    env.ui.setFreeze(id, 0);
            }
            
            penalty = false;
        } catch (InterruptedException ignored) {
        }
           
    }

    public int score() {
        return score;
    }
}
