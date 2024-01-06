package com.spring.testing.test;

import com.spring.testing.util.Card;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.spring.testing.util.Factory.*;

/**
 * Locks lead to deadlocks. The commented code and boolean variables in the
 * tests results a deadlock, which proves the existence of a lock.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LockTests {

    @BeforeEach
    void setUp() {
        beginTransactions();
    }

    /**
     * INSERT INTENTION LOCK
     * <p>
     *     This sets a lock on the row prior to inserting the record and obtaining an exclusive lock.
     * This method tests the hypothesis whether insert intention lock  prevents other transactions
     * from modifying the locked row.
     */
    @Transactional
    @Test
    @Order(1)
    public void intentionLock() {
        em1.createNativeQuery("DELETE FROM t_card_test").executeUpdate(); // makes sure table is empty
        em1.getTransaction().commit();
        em1.getTransaction().begin();
        boolean wantDeadlock = false; // change to true to reach deadlock

        // sets insert intention lock
        em1.createNativeQuery("INSERT INTO t_card_test (id, `rank`, suit) VALUES (1,'Ten', 'Clubs')").executeUpdate();

        if (wantDeadlock) {
            em2.createNativeQuery("UPDATE t_card_test SET suit='Spades' WHERE id=1").executeUpdate();
        }
    }

    /**
     * RECORD EXCLUSIVE LOCK
     * <p>
     *      This method tests the hypothesis whether record lock really prevents other transactions
     * from modifying the locked record.
     */
    @Transactional
    @Test
    public void recordLock() {
        boolean wantDeadlock = false; // change to true to reach deadlock

        em1.merge(CARD_1); // no insert intention lock since this acts like update statement
        em1.flush();

        if (wantDeadlock) {
            em2.createNativeQuery("UPDATE t_card_test SET suit='Spades' WHERE id=1").executeUpdate();
        }

    }

    /**
     * GAP/NEXT-KEY LOCK
     * <p>
     *      For other than unique search conditions, REPEATABLE_READ uses gap locks. This method
     * tests the hypothesis whether REPEATABLE_READ's gap lock will prevent the insertion of a
     * record before another locked one. There are four outcomes, depending on if the search
     * condition is unique and whether the user decides to uncomment the code that leads to a
     * deadlock.
     */
    @Transactional
    @Test
    public void gapLock() {
        boolean isUniqueIndexSearch = false; // RR doesn't use gap locks for unique search
        setIsolationLevel(em1, 2); // REPEATABLE_READ

        em1.merge(CARD_1);
        em1.merge(CARD_3);
        em1.merge(CARD_4);
        em1.flush();
        em1.createNativeQuery(
                "SELECT * FROM t_card_test WHERE id" + (isUniqueIndexSearch ? "=" : "<=") + "3 FOR UPDATE",
                Card.class
        ).getResultList(); // sets gap lock if search condition is not unique

//        em2.merge(CARD_2); // UNCOMMENT to reach deadlock when search condition is not unique
        em2.merge(CARD_5); // no deadlock because no gap lock at index 5
        em2.flush();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        rollbackTransactions();
    }

    @AfterAll
    static void afterAll() {
        closeConnections();
    }
}
