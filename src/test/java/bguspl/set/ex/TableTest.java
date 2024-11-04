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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TableTest {

    Table table;
    private Integer[] slotToCard;
    private Integer[] cardToSlot;

    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    @Mock
    private Dealer dealer;
    @Mock
    private Logger logger;
    
    @BeforeEach
    void setUp() {
       
        slotToCard = new Integer[12];
        cardToSlot = new Integer[81];
       // fillAllSlots();
        // purposely do not find the configuration files (use defaults here).
        Env env = new Env(logger, new Config(logger, ""), ui, util);
        table = new Table(env, slotToCard, cardToSlot);
    }

    private int fillSomeSlots() {
        slotToCard[1] = 3;
        slotToCard[2] = 5;
        cardToSlot[3] = 1;
        cardToSlot[5] = 2;

        return 2;
    }

    private void fillAllSlots() {
        for (int i = 0; i < slotToCard.length; ++i) {
            slotToCard[i] = i;
            cardToSlot[i] = i;
        }
    }

    private void placeSomeCardsAndAssert() throws InterruptedException {
        table.placeCard(8, 2);
        table.placeCard(5, 4);

        assertEquals(8, (int) slotToCard[2]);
        assertEquals(2, (int) cardToSlot[8]);

        verify(ui).placeCard(eq(8), eq(2));

        assertEquals(5, (int) slotToCard[4]);
        assertEquals(4, (int) cardToSlot[5]);

        verify(ui).placeCard(eq(5), eq(4));
    }

    private void placeAllCardsAndAssert() throws InterruptedException{
        for(int i=0; i<slotToCard.length; i++){
            table.placeCard(i, i);
            assertEquals(i, (int) slotToCard[i]);
            assertEquals(i, (int) cardToSlot[i]);

            verify(ui).placeCard(eq(i), eq(i));

        }
    }

    private void removeAllCardsAndAssert() throws InterruptedException{
        for(int i=0; i<slotToCard.length; i++){
            table.removeCard(i);
            assertEquals(null, slotToCard[i]);
            assertEquals(null, cardToSlot[i]);

            verify(ui).removeCard(eq(i));

        }
    }

    private void removeSomeCardsAndAssert() throws InterruptedException{
            table.removeCard(2);
            assertEquals(null, slotToCard[2]);
            assertEquals(null, cardToSlot[5]);

            verify(ui).removeCard(eq(2));

            table.removeCard(1);
            assertEquals(null, slotToCard[1]);
            assertEquals(null, cardToSlot[3]);

            verify(ui).removeCard(eq(1));

        
    }

    @Test
    void countCards_NoSlotsAreFilled() {

        assertEquals(0, table.countCards());
    }

    @Test
    void countCards_SomeSlotsAreFilled() {

        int slotsFilled = fillSomeSlots();
        assertEquals(slotsFilled, table.countCards());
    }

    @Test
    void countCards_AllSlotsAreFilled() {

        fillAllSlots();
        assertEquals(slotToCard.length, table.countCards());
    }

    @Test
    void placeSomeCard_SomeSlotsAreFilled() throws InterruptedException {

        fillSomeSlots();
        placeSomeCardsAndAssert();
    }

    @Test
    void placeSomeCard_AllSlotsAreFilled() throws InterruptedException {
        fillAllSlots();
        placeSomeCardsAndAssert();
    }

    @Test
    void placeAllCard_SomeSlotsAreFilled() throws InterruptedException {
        fillSomeSlots();
        placeAllCardsAndAssert();
    }

    @Test
    void placeAllCard_AllSlotsAreFilled() throws InterruptedException {
        fillAllSlots();
        placeAllCardsAndAssert();
    }

    @Test
    void removeAllCard_AllSlotsAreFilled() throws InterruptedException {
        fillAllSlots();
        removeAllCardsAndAssert();
    }

    @Test
    void removeSomeCard_SomeSlotsAreFilled() throws InterruptedException {
        fillSomeSlots();
        removeSomeCardsAndAssert();
    }

    @Test
    void placeToken() throws InterruptedException {
        fillAllSlots();
        for(int i=0; i<12; i++){
            table.placeToken(1, i);
            verify(ui).placeToken(eq(1), eq(i));
        }
    }

    @Test
    void removeToken() throws InterruptedException {
        fillAllSlots();
        for(int i=0; i<12; i++){
            table.removeToken(1, i);
            verify(ui).removeToken(eq(1), eq(i));
        }
    }
   
}
