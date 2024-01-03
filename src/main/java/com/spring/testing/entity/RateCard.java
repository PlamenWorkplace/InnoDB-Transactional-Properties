package com.spring.testing.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "t_rate_card_test")
public class RateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Timestamp createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private Timestamp updateTime;

    public RateCard() {
    }

    public RateCard(String name, String currency) {
        this.name = name;
        this.currency = currency;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "RateCard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateCard rateCard)) return false;
        return getId() == rateCard.getId() && Objects.equals(getName(), rateCard.getName()) && Objects.equals(getCurrency(), rateCard.getCurrency());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getCurrency(), getCreateTime(), getUpdateTime());
    }
}
