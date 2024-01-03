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
in the making
# Isolation
### READ_UNCOMMITTED vs READ_COMMITTED <br>
  PUT <br>
  http://localhost:8080/isolation/one?is-read-committed={true|false} <br/>

Sometimes T1 may detect a change in an object's state made by another concurrent T2, before it ever commits.
Even after T2 rollbacks, it may have already been read by T1.
So a transaction may have the ability to read a record that never existed in the database because another concurrent
transaction made a change that never committed.
This is called a dirty read.
READ_COMMITTED prevents such occurrences.
<Br/><br/>
### READ_COMMITTED vs REPEATABLE_READ <br>
  PUT <br>
  http://localhost:8080/isolation/two?is-repeatable-read={true|false} <br/>

Sometimes an object's state is not consistent in the scope of an ongoing transaction.
Suppose T1 reads a record multiple times, and meanwhile T2 decides to change its state and commit.
Later depending on the isolation level, T1 may detect the update and read the updated object.
This is called a non-repeatable read.
REPEATABLE_READ prevents such occurrences.
<Br/><br/>
### REPEATABLE_READ vs SERIALIZABLE <br>
  PUT <br>
  http://localhost:8080/isolation/three?is-serializable={true|false}&is-mysql={true|false} <br/>

This request tests when a phantom read can occur.
This is when a range of records don't stay consistent in the scope of an ongoing transaction.
Suppose T1 reads a range of records multiple times, and meanwhile T2 changes a row between the same records.
Depending on the isolation level, T1 may detect the change and read different records.
This is called a phantom read.
<Br/><Br/>
Normally, SERIALIZABLE would prevent such occurrences, but in MySQL, REPEATABLE_READ doesn't produce it either.
READ_COMMITTED also will not produce phantom reads.
Interestingly, MySQL hides the phantom read, so it can still be reproduced, and that is what the 'is-mysql' 
query tries to simulate.
<Br/>
Suppose T1 makes another update on the same object that concurrent T2 just updated. 
Only then a phantom read will occur.
Interestingly, that happens only the first time, and never again, if the connection stays open.
But if before each request you open the connection, aka entity manager, and in the end you close it, phantom read 
will always occur.
This means that it always occurs the first time a connection is established (after the random update), 
and if the connection stays open, it never happens again.
So investigations show that a phantom read is very unlikely to ever happen on isolation levels stronger or
equal than READ_COMMITTED.
<Br/><Br/>
It is also important to note that in comparison to REPEATABLE_READ, SERIALIZABLE creates a shared (read-only) lock
on the object at hand, which is released after the transaction commits.
So a deadlock will occur when T2 tries to update objects that T1 has a lock on.
<Br/><Br/>

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