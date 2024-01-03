# Prerequisites
To start the application and test different propagation and isolation levels in MySQL, one needs to set
the following environmental variables:
<br/>
* DB_URL - schema URL
* DB_USER - user name
* DB_PASS - user password <br/>

He should also create t_rate_card_test in advance. Table's definition is present in the Appendix below.
The table will be populated when the first request occurs, although the user may decide to populate it
manually.

# Propagation
burabura
# Isolation
MySQL Documentation provides a comprehensive explanation when to use different isolation levels. The endpoints
described below show the comparisons and the consequences of every isolation level.
### READ_UNCOMMITTED vs READ_COMMITTED
### READ_COMMITTED vs REPEATABLE_READ
### REPEATABLE_READ vs SERIALIZABLE
It is important to note that in comparison to REPEATABLE_READ, SERIALIZABLE creates a shared (read-only) lock
on the object at hand, which is released after the transaction commits. So a deadlock will occur when T2 tries to
update objects that T1 has a lock on. <Br/>
Another interesting phenomenon. 

#### General Uses
READ_UNCOMMITTED -  <br/>
READ_COMMITTED -  <br/>
REPEATABLE_READ -   <br/>
**SERIALIZABLE** - not used a lot, mainly for distributed transactions between multiple databases, and debugging issues
with concurrency and deadlocks.  <br/>

# Appendix
CREATE TABLE `t_rate_card_test` ( <br/>
`id` bigint NOT NULL AUTO_INCREMENT, <br/>
`name` varchar(255) NOT NULL, <br/>
`currency` varchar(255) NOT NULL, <br/>
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, <br/>
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, <br/>
PRIMARY KEY (`id`), <br/>
KEY `indx_rc_name` (`name`), <br/>
KEY `indx_rc_ct` (`create_time`), <br/>
KEY `indx_rc_ut` (`update_time`) <br/>
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci; <br/><br/>