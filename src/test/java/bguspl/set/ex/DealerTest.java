package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealerTest {


    Dealer dealer;
    private Player[] players;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;

    private Table table;

    private Integer[] slotToCard;
    private Integer[] cardToSlot;

    @Mock
    private Logger logger;


    void assertInvariantsBefore() {
        assertEquals(81, (int)dealer.countDeck());
    }

    void assertInvariants() {
        assertEquals(81, (int)dealer.countDeck());
    }

    private void createMockPlayers(Env env)
    {
        for(int i = 0; i < 2; i++)
        {
            players[i] = new Player(env, dealer, table, i, false);
        }
        players[0].point();
        players[0].point();
        players[1].point();
    }

    @BeforeEach
    void setUp() {
        players = new Player[2];
        slotToCard = new Integer[12];
        cardToSlot = new Integer[81];
        // purposely do not find the configuration files (use defaults here).
        Env env = new Env(logger, new Config(logger, (String) null), ui, util);
        createMockPlayers(env);
        table = new Table(env, slotToCard, cardToSlot);
        dealer = new Dealer(env, table, players);
        System.out.println(dealer.countDeck());

        assertInvariants();
    }

    @AfterEach
    void tearDown() {
        assertInvariants();
    }

    
    @Test
    void placeAndremoveAllCardsFromTableTest() {

        dealer.placeCardsOnTableForTest();

        // call the method we are testing
        dealer.removeAllCardsFromTableForTest();
        
        for(int i = 0; i < 12; i++)
        {
            // check that the cards in table removed successfully
            assertNull(table.slotToCard[i]);
        }
        
        for(int i=0; i<80; i++){
            assertNull(table.cardToSlot[i]);
        }

        // check that ui.removeCard was called for each slot
        for(int i = 0; i < 12; i++)
        {
            verify(ui).removeCard(eq(i));
        }
    }

    @Test
    void announceWinners(){

        dealer.announceWinnersForTests();

        int[] winner = new int[1];

        winner[0] = 0;

        verify(ui).announceWinner(eq(winner));;

    }
    
}