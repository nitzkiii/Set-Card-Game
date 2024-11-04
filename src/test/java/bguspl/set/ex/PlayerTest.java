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
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    Player player;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    
    private Table table;

    private Integer[] slotToCard;
    private Integer[] cardToSlot;

    @Mock
    private Dealer dealer;
    @Mock
    private Logger logger;
    
    void assertInvariants() {
        assertTrue(player.id >= 0);
        assertTrue(player.score() >= 0);
    }

    @BeforeEach
    void setUp() {
        slotToCard = new Integer[12];
        cardToSlot = new Integer[81];
        fillAllSlots();
        // purposely do not find the configuration files (use defaults here).
        Env env = new Env(logger, new Config(logger, ""), ui, util);
        table = new Table(env, slotToCard, cardToSlot);
        player = new Player(env, dealer, table, 0, false);
        assertInvariants();
    }

    private void fillAllSlots() {
        for (int i = 0; i < slotToCard.length; ++i) {
            slotToCard[i] = i;
            cardToSlot[i] = i;
        }
    }
   
    @AfterEach
    void tearDown() {
        assertInvariants();
    }

    @Test
    void point() {

        // calculate the expected score for later
        int expectedScore = player.score() + 1;

        // call the method we are testing
        player.point();

        // check that the score was increased correctly
        assertEquals(expectedScore, player.score());

        // check that ui.setScore was called with the player's id and the correct score
        verify(ui).setScore(eq(player.id), eq(expectedScore));
    }

    @Test
    void penalty() {
        // calculate the expected score for later
        int expectedScore = player.score();

        // call the method we are testing
        player.penalty();

        // check that the score remained the same
        assertEquals(expectedScore, player.score());
    }
    
    @Test
    void keyPressed_addSlot(){
       
        for(int i=0; i<3; i++){
            int slot = i;

            // call the method we are testing
            player.keyPressed(slot);

            // check that the slot added to the actions list
            assertEquals(slot, player.actions.get(i));

            // check that ui.placeToken was called with the player's id and the correct slot
            verify(ui).placeToken(eq(player.id), eq(slot));
        }
    }

    @Test
    void keyPressed_removeSlot(){
       
        for(int i=0; i<3; i++){
            player.actions.add(i);
        }

        for(int i=0; i<3; i++){
            int slot = i;

            // call the method we are testing
            player.keyPressed(slot);
          
            // check that the slot revmoved from the actions list
            assertEquals(3-i-1, player.actions.size());

            // check that ui.removeToken was called with the player's id and the correct slot
            verify(ui).removeToken(eq(player.id), eq(slot));
        }
    }

}