package com.spring.testing.test;

import com.spring.testing.util.Card;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.spring.testing.util.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IsolationTests {

    private static final String CONCLUSION_1 = "T2 detected the record inserted by T1. Dirty read";
    private static final String CONCLUSION_2 = "T2 didn't read the T1's inserted record";
    private static final String CONCLUSION_3 = "The previous snapshot was read instead of the new value";
    private static final String CONCLUSION_4 = "New value was read. Non-repeatable read occurred";

    @BeforeEach
    void setUp() {
        em1.getTransaction().begin();
        em1.createNativeQuery("DELETE FROM t_card_test").executeUpdate();
        em1.getTransaction().commit();
        beginTransactions();
    }

    /**
     * 1. READ_UNCOMMITTED vs READ_COMMITTED
     * <p>
     *      Two transactions take place, from two separate connections. T1 inserts a card and
     * T2 tries to read it. Depending on the set isolation level, T2 may be able to read the
     * card (dirty read) or it may not.
     */
    @Transactional
    @Test
    public void readUncommittedTestOne() {
        assertEquals(CONCLUSION_1, testOne(false));
    }

    @Transactional
    @Test
    public void readCommittedTestOne() {
        assertEquals(CONCLUSION_2, testOne(true));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String testOne(boolean isReadCommitted) {
        em1.createNativeQuery("INSERT INTO t_card_test (id, `rank`, suit) VALUES (1,'Ten', 'Clubs')").executeUpdate();

        setIsolationLevel(em2, isReadCommitted ? 1 : 0);
        String output;
        try {
            em2.createQuery("SELECT c FROM Card c WHERE id=1L", Card.class).getSingleResult();
            output = CONCLUSION_1;
        } catch (NoResultException e) {
            output = CONCLUSION_2;
        }
        rollbackTransactions();
        return output;
    }

    /**
     * 2. READ_COMMITTED vs REPEATABLE_READ
     * <p>
     *      Two transactions take place, from two separate connections. T1 inserts a card and
     * selects it (no commit). Later T2 updates the record, and commits it. Depending on T1's
     * isolation level, it may be able to read the change made by T2 (non-repeatable read) or
     * it may not.
     */
    @Transactional
    @Test
    public void readCommittedTestTwo() {
        assertEquals(CONCLUSION_4, testTwo(false));
    }

    @Transactional
    @Test
    public void repeatableReadTestTwo() {
        assertEquals(CONCLUSION_3, testTwo(true));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String testTwo(boolean isRepeatableRead) {
        setIsolationLevel(em1, isRepeatableRead ? 2 : 1);
        em1.merge(CARD_1);
        em1.getTransaction().commit();
        em1.getTransaction().begin();
        String initialSuit = em1.createNativeQuery("SELECT suit FROM t_card_test WHERE id=1").getSingleResult().toString();

        em2.createNativeQuery("UPDATE t_card_test SET suit='Spades' WHERE id=1").executeUpdate();
        em2.getTransaction().commit();

        String updatedSuit = em1.createNativeQuery("SELECT suit FROM t_card_test WHERE id=1").getSingleResult().toString();
        em1.getTransaction().rollback();

        if (initialSuit.equals(updatedSuit)) {
            return CONCLUSION_3;
        } else {
            return CONCLUSION_4;
        }
    }

    /**
     * 3. REPEATABLE_READ vs SERIALIZABLE
     * <p>
     *      No difference between these two, only that in SERIALIZABLE, all SELECT statements
     * are converted to SELECT ... FOR SHARE, thus creating a shared lock, and increasing the
     * chance of reaching a deadlock.
     */

    @AfterAll
    static void afterAll() {
        closeConnections();
    }
}
