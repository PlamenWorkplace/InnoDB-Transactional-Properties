# Prerequisites
To test some of MySQL's specific functionalities, one should set the following environmental variables:
<br/>
* DB_URL - schema URL
* DB_USER - user name
* DB_PASS - user password <br/>

You should also create t_card_test in advance.
Table's definition is present in the Appendix below.
No further setup is necessary.

# Propagation
in the making
# Isolation
### READ_UNCOMMITTED vs READ_COMMITTED <br>

Sometimes T1 may detect a change in an object's state made by another concurrent T2.
Even after T2 rollbacks, it may have already been read by T1.
So a transaction may have the ability to read a record that never existed in the database because another concurrent
transaction made a change that was never committed.
This is called a dirty read.
READ_COMMITTED prevents such occurrences.
<Br/>
### READ_COMMITTED vs REPEATABLE_READ <br>

Sometimes an object's state is not consistent in the scope of an ongoing transaction.
Suppose T1 reads a record multiple times, and meanwhile T2 decides to change its state and commits.
Later, T1 may be able to detect the update and read the updated object.
This is called a non-repeatable read.
REPEATABLE_READ prevents such occurrences.
<Br/>
### REPEATABLE_READ vs SERIALIZABLE <br>

In comparison to REPEATABLE_READ, SERIALIZABLE creates a shared (read-only) lock
on the object at hand, which is released after the transaction commits.
So a deadlock will occur when T2 tries to update objects that T1 has a shared lock on.
Other than that, there's no difference between the two.
<Br/>
# Lock
### Shared and Exclusive Lock <br>
MySQL supports standard locking techniques.
When a transaction obtains a shared lock on a database object,
it can only read the row, and other concurrent transactions are also only permitted to read it.
On the other hand, when a transaction obtains an exclusive lock on a database object,
it can read it and modify it as he wants to.
On the other hand, other concurrent transactions have no access to the record until the lock is released.
### Record Lock <br>
Record lock is a lock on an indexed record. It can be shared or exclusive.
### Gap Lock <br>
Gap lock is a lock on the gaps between indexed records.
Gap locks span before the first and after the last indexed record.
### Next-Key Lock <br>
Next-Key lock locks the record and the gap before it.
Thus, it is a combination of a record lock and gap lock.
This is the type of lock REPEATABLE_READ uses when selecting records by a non-unique condition.
### Insert Intention Lock <br>
Insert Intention lock sets a lock on the row before a record is inserted in the table.
Helps in inserting records to non-conflicting rows.

# Appendix
CREATE TABLE `t_card_test` ( <br/>
`id` bigint NOT NULL, <br/>
`rank` varchar(255) NOT NULL,<br/>
`suit` varchar(255) NOT NULL,<br/>
PRIMARY KEY (`id`) <br/>
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci; <br/><br/>