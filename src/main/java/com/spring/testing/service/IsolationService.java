package com.spring.testing.service;

import com.spring.testing.entity.RateCard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.List;

@Service
public class IsolationService {

    private final EntityManagerFactory emf;

    // Session 1
    private EntityManager em1;

    // Session 2
    private EntityManager em2;

    @Autowired
    public IsolationService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void setup() {
        openSessions();
    }

    /**
     * READ_UNCOMMITTED vs READ_COMMITTED
     * <p>
     *      First and foremost, there should be a rate card in the database, so one is created
     * if it doesn't exist.
     *      Two transactions take place, from two separate connections. T1 updates a rate card,
     * but doesn't commit. After that, a second connection starts a concurrent T2, initiating a
     * read on that card. Depending on the set isolation level, T2 may be able to read the update,
     * which is called a dirty read. In the end, T1 gets a rollback, so the update is lost.
     *
     * @param isReadCommitted sets the second connection's isolation level
     * @return if dirty read occurred
     */
    @Transactional
    public String exampleOne(boolean isReadCommitted) {
//        openSessions();
        RateCard rateCard = findRateCard(em1);
        em1.getTransaction().begin();
        updateRateCard(em1, rateCard.getName(), rateCard.getId());

        setIsolationLevel(em2, isReadCommitted ? 1 : 0);
        String name = em2.createQuery("SELECT name FROM RateCard WHERE id=" + rateCard.getId())
                .getSingleResult().toString();

        em1.getTransaction().rollback();
//        closeSessions();
        if (name.equals(rateCard.getName())) {
            return "UNCOMMITTED UPDATE IN T1 WAS NOT READ BY CONCURRENT T2";
        } else {
            return "DIRTY READ OCCURRED";
        }
    }

    /**
     * READ_COMMITTED vs REPEATABLE_READ
     * <p>
     *      First and foremost, there should be a rate card in the database, so one is created
     * if it doesn't exist.
     *      Two transactions take place, from two separate connections. The first connection's
     * isolation is set based on
     * @param isRepeatableRead sets the first connection's isolation level
     * @return if ...
     */
    @Transactional
    public String exampleTwo(boolean isRepeatableRead) {
//        openSessions();
        long rateCardId = findRateCard(em1).getId();

        setIsolationLevel(em1, isRepeatableRead ? 2 : 1);
        em1.getTransaction().begin();
        String name1 = em1.createQuery("SELECT name FROM RateCard WHERE id=" + rateCardId).getSingleResult().toString();

        em2.getTransaction().begin();
        updateRateCard(em2, name1, rateCardId);
        em2.getTransaction().commit();

        String name2 = em1.createQuery("SELECT name FROM RateCard WHERE id=" + rateCardId).getSingleResult().toString();
        em1.getTransaction().commit();

//        closeSessions();
        if (name1.equals(name2)) {
            return "UPDATED RECORD BY T2 COULD NOT BE READ BY CONCURRENT T1. OLDER VERSION WAS READ INSTEAD.";
        } else {
            return "NON-REPEATABLE READ OCCURRED";
        }
    }

    /**
     * REPEATABLE_READ vs SERIALIZABLE
     *
     * @param isSerializable sets the first connection's isolation level
     * @return if ...
     */
    @Transactional
    public String exampleThree(boolean isSerializable, boolean isMySQL) {
        if (isSerializable) {
            return "SERIALIZABLE in T1 will create a shared lock. Updating the record by the " +
                    "concurrent T2 will lead to a deadlock. AVOID AT ALL COST.";
        }
        RateCard rateCardToUpdate = findRateCard(em1);

        setIsolationLevel(em1, 2);
        em1.getTransaction().begin();
        String initialName = em1.createQuery(
                "SELECT name FROM RateCard rc WHERE id<=" + rateCardToUpdate.getId(), String.class
        ).getResultList().get(1);

        em2.getTransaction().begin();
        updateRateCard(em2, rateCardToUpdate.getName(), rateCardToUpdate.getId());
        em2.getTransaction().commit();

        if (!isMySQL) {
            randomRateCardUpdate(em1, rateCardToUpdate);
        }
        String updatedName = em1.createQuery(
                "SELECT name FROM RateCard rc WHERE id<=" + rateCardToUpdate.getId(), String.class
        ).getResultList().get(1);
        em1.getTransaction().commit();

        if (initialName.equals(updatedName)) {
            return "PHANTOM READ DID NOT OCCUR";
        } else {
            return "PHANTOM READ OCCURRED";
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateRateCard(EntityManager em, String name, long id) {
        if (name.equals("A1")) {
            em.createQuery("UPDATE RateCard SET name='Vodafone' WHERE id=" + id).executeUpdate();
        } else {
            em.createQuery("UPDATE RateCard SET name='A1' WHERE id=" + id).executeUpdate();
        }
        em.flush();
    }

    /*
    This is the most random update on the already updated by T2 rate card. MySQL hides
    the phantom read, but it can still be reproduced by making another update by T1 on
    the same record. No explanation to why this works...
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void randomRateCardUpdate(EntityManager em, RateCard rateCard) {
        if (rateCard.getCurrency().equals("BGN")) {
            em.createQuery("UPDATE RateCard SET currency='EU' WHERE id=" + rateCard.getId()).executeUpdate();
        } else {
            em.createQuery("UPDATE RateCard SET currency='BGN' WHERE id=" + rateCard.getId()).executeUpdate();
        }
        em.flush();
    }

    /*
    This will select the second rate card from all rate cards in the database, ordered by id.
    Since we need a range of objects for the phantom read, the first and the second rate cards
    will be selected. The second one will be modified and tested if the update was detected by
    the other transaction.
    */
    @Transactional(propagation = Propagation.NEVER)
    public RateCard findRateCard(EntityManager em) {
        em.getTransaction().begin();
        List<RateCard> rateCards = em.createQuery("SELECT rc FROM RateCard rc ORDER BY id", RateCard.class).getResultList();
        if (rateCards.size() <= 1) {
            em.persist(new RateCard("A1", "BGN"));
            em.persist(new RateCard("A1", "BGN"));
            rateCards = em.createQuery("SELECT rc FROM RateCard rc ORDER BY id", RateCard.class).getResultList();
        }
        em.getTransaction().commit();
        return rateCards.get(1);
    }

    private void setIsolationLevel(EntityManager em, int i) {
        em.unwrap(Session.class).doWork(connection -> {
            if (i == 0) {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            } else if (i == 1){
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } else if (i == 2){
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            }
        });
    }

    @EventListener(ContextStoppedEvent.class)
    public void teardown() {
        closeSessions();
    }

    private void openSessions() {
        em1 = emf.createEntityManager();
        em2 = emf.createEntityManager();
    }

    private void closeSessions() {
        em1.close();
        em2.close();
    }
}
