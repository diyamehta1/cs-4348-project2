# CS4348 Project 2 - Bank Simulation

## Project Overview
Project based upon Project 2 Guidelines given to us. 

## Files

- **BankSimulationProject2.java** - Main program file containing:
  - `Teller` class- Implements teller thread behavior
  - `Customer` class- Implements customer thread behavior
  - Main method - Initializes and starts all threads, manages shutdown
  - Synchronization primitives and shared data structures

- **devlog.md** - My dev log including development procedures & timelines 
- **README.md** - this file. 

## Compilation and Execution

### On any system w/ Java installed: 

**Compile:**
```bash
javac BankSimulationProject2.java
```

**Run:**
```bash
java BankSimulationProject2
```

### Expected Behavior

The program will:
1. Start 3 teller threads that announce they're ready to operate 
2. Start 50 customer threads that randomly choose deposit or withdrawal transactions
3. Synchronize customer-teller interactions through semaphores
4. Enforce resource limits:
   - Max 2 customers in door area at once
   - Max 2 tellers in safe at once
   - Max 1 teller with manager at once
5. Print formatted output showing all actions
6. Cleanly shut down after all customers are gotten to
7. Print "The bank closes for the day" at the very end. 

### Output Format

All output follows the formatting given to us. 


## Implementation Details

### Synchronization Mechanisms

- **Semaphores:**
  - `door` : Controls customer entry to bank
  - `safe` : Controls teller access to safe
  - `manager` : Controls teller access to manager
  - Per-teller semaphores: Allow customers to signal teller assignment
  - Per-customer semaphores: Coordinate teller-customer communication

- **LinkedBlockingQueue:** Manages available teller assignments

- **CountDownLatch:** Ensures bank opens only after all tellers are ready

- **AtomicBoolean:** Thread-safe closing flag for coordination of shutdown 

### Thread Workflows

**Teller:**
1. Announces ready to serve
2. Adds self to available tellers' line
3. Waits for customer assignment
4. Asks customer for transaction
5. Handles withdrawal or deposit
6. Visits safe to complete transaction
7. Tells customer abt completion 
8. Waits for customer to exit 
9. Returns to available pool, unless closing 

**Customer:**
1. Randomly chooses deposit or withdrawal
2. Waits random time 
3. Waits for bank to open
4. Enters through door (semaphore)
5. Selects available teller
6. Provides details of transaction 
7. Waits for teller to finish transaction
8. Leaves teller and exits bank
9. Releases door semaphore


