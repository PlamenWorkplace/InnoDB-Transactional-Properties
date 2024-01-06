package com.spring.testing.util;

import jakarta.persistence.*;

@Entity
@Table(name = "t_card_test")
public class Card {

    @Id
    @Column
    private long id;

    @Column(name = "`rank`", nullable = false)
    private String rank;

    @Column(nullable = false)
    private String suit;

    public Card() {
    }

    public Card(long id, String rank, String suit) {
        this.id = id;
        this.rank = rank;
        this.suit = suit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }
}
