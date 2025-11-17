import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class BankSimulationProject2 {
    static final int NUM_TELLERS = 3;
    static final int NUM_CUSTOMERS = 50;

    static final Semaphore door = new Semaphore(2);
    static final Semaphore safe = new Semaphore(2);
    static final Semaphore manager = new Semaphore(1);

    static final LinkedBlockingQueue<Integer> availableTellers = new LinkedBlockingQueue<>();

    static final Semaphore[] tellerSem = new Semaphore[NUM_TELLERS];

    static final Semaphore[] askTrans = new Semaphore[NUM_CUSTOMERS];
    static final Semaphore[] transGiven = new Semaphore[NUM_CUSTOMERS];
    static final Semaphore[] tellerDone = new Semaphore[NUM_CUSTOMERS];
    static final Semaphore[] customerLeft = new Semaphore[NUM_CUSTOMERS];

    static final int[] assignedCustomer = new int[NUM_TELLERS];
    static final int[] transactionType = new int[NUM_CUSTOMERS];

    static final CountDownLatch openLatch = new CountDownLatch(NUM_TELLERS);

    static final AtomicBoolean closing = new AtomicBoolean(false);

    static final AtomicInteger servedCount = new AtomicInteger(0);

    static final Object printLock = new Object();

    static String otherFmt(String type, int id, boolean empty) {
        if (empty) return "[]";
        return String.format("[%s %d]", type, id);
    }

    static void println(String threadType, int id, String otherType, Integer otherId, String msg) {
        String other;
        if (otherType == null) other = "[]";
        else other = String.format("[%s %d]", otherType, otherId);
        synchronized (printLock) {
            System.out.printf("%s %d %s: %s\n", threadType, id, other, msg);
        }
    }

    static class Teller implements Runnable {
        private final int id;

        Teller(int id) {
            this.id = id;
            assignedCustomer[id] = -1;
        }

        @Override
        public void run() {
            try {
                println("Teller", id, null, null, "ready to serve");
                openLatch.countDown();
                availableTellers.put(id);
                println("Teller", id, null, null, "waiting for a customer");

                while (true) {
                    tellerSem[id].acquire();

                    if (closing.get() && assignedCustomer[id] == -1) break;

                    int custId;
                    synchronized (this) {
                        custId = assignedCustomer[id];
                    }
                    if (custId == -1) {
                        if (closing.get()) break;
                        else continue;
                    }

                    println("Teller", id, "Customer", custId, "serving a customer");
                    println("Teller", id, "Customer", custId, "asks for transaction");

                    askTrans[custId].release();

                    transGiven[custId].acquire();

                    int ttype = transactionType[custId];
                    if (ttype == 1) {
                        println("Teller", id, "Customer", custId, "handling withdrawal transaction");
                        println("Teller", id, "Customer", custId, "going to the manager");
                        println("Teller", id, "Customer", custId, "getting manager's permission");

                        manager.acquire();
                        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 31));
                        println("Teller", id, "Customer", custId, "got manager's permission");
                        manager.release();
                    } else {
                        println("Teller", id, "Customer", custId, "handling deposit transaction");
                    }

                    println("Teller", id, "Customer", custId, "going to safe");
                    println("Teller", id, "Customer", custId, "enter safe");

                    safe.acquire();
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 51));
                    } finally {
                        println("Teller", id, "Customer", custId, "leaving safe");
                        safe.release();
                    }

                    println("Teller", id, "Customer", custId, "finishes " + (ttype==1?"withdrawal":"deposit") + " transaction.");
                    println("Teller", id, "Customer", custId, "wait for customer to leave.");

                    tellerDone[custId].release();

                    customerLeft[custId].acquire();

                    synchronized (this) {
                        assignedCustomer[id] = -1;
                    }

                    int s = servedCount.incrementAndGet();

                    if (!closing.get()) {
                        availableTellers.put(id);
                        println("Teller", id, null, null, "ready to serve");
                        println("Teller", id, null, null, "waiting for a customer");
                    } else {
                        break;
                    }
                }
            } catch (InterruptedException e) {
            } finally {
                println("Teller", id, null, null, "leaving for the day");
            }
        }
    }

    static class Customer implements Runnable {
        private final int id;

        Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                transactionType[id] = ThreadLocalRandom.current().nextInt(0, 2);
                println("Customer", id, null, null, "wants to perform a " + (transactionType[id]==0?"deposit":"withdrawal") + " transaction");

                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 101));

                openLatch.await();

                door.acquire();
                println("Customer", id, null, null, "going to bank.");
                println("Customer", id, null, null, "entering bank.");

                println("Customer", id, null, null, "getting in line.");

                int tellerId = availableTellers.take();

                println("Customer", id, null, null, "selecting a teller.");
                println("Customer", id, "Teller", tellerId, "selects teller");
                println("Customer", id, "Teller", tellerId, "introduces itself");

                synchronized (tellerSem[tellerId]) {
                    assignedCustomer[tellerId] = id;
                    tellerSem[tellerId].release();
                }

                askTrans[id].acquire();

                println("Customer", id, "Teller", tellerId, "asks for " + (transactionType[id]==0?"deposit":"withdrawal") + " transaction");
                transGiven[id].release();

                tellerDone[id].acquire();

                println("Customer", id, "Teller", tellerId, "leaves teller");
                println("Customer", id, null, null, "goes to door");
                println("Customer", id, null, null, "leaves the bank");

                customerLeft[id].release();

                door.release();

            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < NUM_TELLERS; i++) tellerSem[i] = new Semaphore(0);
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            askTrans[i] = new Semaphore(0);
            transGiven[i] = new Semaphore(0);
            tellerDone[i] = new Semaphore(0);
            customerLeft[i] = new Semaphore(0);
        }

        Thread[] tellers = new Thread[NUM_TELLERS];
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellers[i] = new Thread(new Teller(i));
            tellers[i].start();
        }

        Thread[] customers = new Thread[NUM_CUSTOMERS];
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customers[i] = new Thread(new Customer(i));
            customers[i].start();
        }

        for (int i = 0; i < NUM_CUSTOMERS; i++) customers[i].join();

        closing.set(true);
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellerSem[i].release();
        }

        for (int i = 0; i < NUM_TELLERS; i++) tellers[i].join();

        synchronized (printLock) {
            System.out.println("The bank closes for the day.");
        }
    }
}