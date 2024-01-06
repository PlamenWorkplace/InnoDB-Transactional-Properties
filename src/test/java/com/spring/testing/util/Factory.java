package com.spring.testing.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class Factory {

    public static final Card CARD_1 = new Card(1L, "Ten", "Clubs");
    public static final Card CARD_2 = new Card(2L, "Jack", "Spades");
    public static final Card CARD_3 = new Card(3L, "Queen", "Diamonds");
    public static final Card CARD_4 = new Card(4L, "King", "Hearts");
    public static final Card CARD_5 = new Card(5L, "Ace", "Spades");

    // Session 1
    public static EntityManager em1;

    // Session 2
    public static EntityManager em2;

    @Autowired
    public Factory(EntityManagerFactory emf) {
        em1 = emf.createEntityManager();
        em2 = emf.createEntityManager();
    }

    public static void beginTransactions() {
        em1.getTransaction().begin();
        em2.getTransaction().begin();
    }

    public static void rollbackTransactions() {
        em1.getTransaction().rollback();
        em2.getTransaction().rollback();
    }

    public static void setIsolationLevel(EntityManager em, int i) {
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

    public static void closeConnections() {
        em1.close();
        em2.close();
    }
}
