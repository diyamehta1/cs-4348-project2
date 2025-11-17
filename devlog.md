# Dev Log 

## 2025-11-11 14:30

## Beginning Thoughts 

Started work on the project
- 3 different teller threads
- 50 customer threads
- Synchronization using semaphores for door (2 customers), safe (2 tellers) and manager

Biggest challenge is coordinating all this so that it works efficiently/properly 

### Overall Plan

1. Implement basic thread structure outlined in the doc 
2. Set up synchronization primitives 
3. Implement the teller workflow, including waiting for customer, asking for transaction, etc, etc 
4. Implement the customer workflow, including talking to the teller, etc, etc 
5. Ensure that it begins and ends properly 
6. Test using multiple scenarios to prevent issues 
7. Verify output format matches doc

The project requires careful attention to the order of semaphore acquisitions and releases to avoid deadlocks.

## 2025-11-12 16:20

### Thoughts So Far

Been thinking about the synchronization strategy. The key insight is that each customer needs their own set of coordination semaphores to communicate with whatever teller gets to them.  Can't share these across customers or we'll have race conditions.

### Session Goals

This session I plan to:
1. Create the basic project structure and file
2. Set up all constants and synchronization primitives
3. Implement the main method skeleton
4. Initialize all semaphore arrays

### Work Done

Created BankSimulationProject2.java with:
- All required constants
- Main synchronization semaphores
- Per-customer semaphore arrays for coordination
- Per-teller semaphore array
- LinkedBlockingQueue for available tellers
- CountDownLatch for bank opening synchronization
- AtomicBoolean for closing flag
- Shared data arrays 

Set up the main method to initialize all semaphore arrays & to prepare for semaphore creation. 

### End of Session Reflection

Good progress on the foundation. Have all the data structures in place. Next session will focus on implementing the Teller class logic. Need to be careful about the order of semaphore operations to avoid deadlock.

## 2025-11-13 14:10

### Thoughts So Far

Reviewed the project specification again. The teller workflow is complex - need to handle both deposit and withdrawal transactions differently. Withdrawals require manager permission before going to the safe.

### Session Goals

This session I plan to:
1. Implement the Teller class
2. Write the complete  teller workflow including manager and safe interactions
3. Add proper print  statements with correct formatting
4. Handle the teller's states 

### Work Done

Implemented the Teller class as a Runnable:
- Teller announces ready to serve and counts down the latch
- Adds itself to availableTellers queue
- Waits on its own semaphore for customer assignment
- When customer arrives, asks for transaction
- Handles withdrawal by visiting manager. 
- All transactions visit the safe 
- Proper timing with Thread.sleep() for manager  and safe 
- Signals customer when done and waits for customer to leave
- Returns to available pool unless bank is closing

Added synchronized blocks to protect assignedCustomer reads/writes.

Implemented print format 

### Challenges

The closing logic is tricky. When the bank closes, tellers might be waiting on their semaphore. Need to wake them up explicitly so they can exit their loop. Used the closing flag to signal this state.

### End of Session Reflection

Teller class is complete and handles all the required workflow steps. Next session will implement the Customer class to complete the simulation.

## 2025-11-14 18:30

### Thoughts So Far

Now that tellers are implemented, need to work on the customer side. Customers need to coordinate carefully with tellers through the semaphore handshakes.

### Session Goals

This session I plan to:
1. Implement the Customer class
2. Write the complete customer workflow
3. Ensure proper coordination with tellers through semaphores
4. Handle door entry/exit
5. Add all required print statements

### Work Done

Implemented the Customer class as a Runnable:
- Randomly decides on deposit or withdraw 
- Waits random time 0-100ms before going to bank
- Waits for bank to open (await on CountDownLatch)
- Acquires door semaphore (max 2 customers)
- Gets in line by taking a teller from availableTellers queue
- Assigns itself to that teller and signals the teller
- Waits for teller to ask for transaction (askTrans semaphore)
- Provides transaction type 
- Waits for teller to complete work 
- Leaves teller and bank (releases customerLeft semaphore)
- Releases door semaphore on exit

Added all required print statements showing customer actions and teller interactions.

### Challenges

Had to be careful about the order of semaphore operations. Customer must signal teller PRIOR waiting for askTrans, otherwise deadlock. The four-semaphore handshake ensures proper handling. 

### End of Session Reflection

Customer class complete. The handshake between customer and teller should work correctly now. Next session will finish the main method and test everything together.

## 2025-11-15 20:15

### Thoughts So Far

Almost done! Need to complete the main method to start all threads and handle cleanup. Also need to make sure the closing sequence works properly so tellers don't hang.

### Session Goals

This session I plan to:
1. Complete the main method threads creation. 
2. Implement proper shutdown sequence
3. Test the full simulation.
4. Fix any deadlocks or race conditions
5. Verify output format matches specification

### Work Done

Completed main method:
- Initialized all semaphore arrays in loops
- Creates and starts 3 teller threads. 
- Creates and starts 50 customer threads
- Joins on all customer threads (waits for them to finish)
- Sets closing flag when all the customers are finished. 
- Releases all teller semaphores to wake them up
- Joins on all teller threads
- Prints "The bank closes for the day."

### Testing Results

Ran the simulation multiple times. Observed:
- All 3 tellers announce ready and wait
- Bank opens after the tellers are all ready. 
- Customers enter in waves (door limits to 2)
- Tellers serve customers properly
- Withdrawals visit manager prior to safe
- Deposits go right to the safe 
- Max 2 tellers in safe at once
- Max 1 teller with manager at once
- All customers served and exit
- All tellers leave properly. 
- Final message prints

No deadlocks detected after multiple runs, output matches the doc. 

### Minor Fixes

Added synchronized block surrounding final print to ensure it is not interleaved with any teller "leaving for the day" messages.

Added printLock object to synchronize all println calls and prevent line interleaving. 

### End of Session Reflection

Simulation is complete and working correctly, all synchronization primitves working fine. 
- Door semaphore limits customers entering
- Safe semaphore limits tellers in safe
- Manager semaphore limits teller-manager interactions
- Per-customer semaphores coordinate handshakes
- CountDownLatch ensures proper bank opening
- AtomicBoolean enables clean shutdown

The program successfully demonstrates complex multi-threaded synchronization without deadlocks or race conditions.

## 2025-11-16 13:45

### Thoughts So Far

Did a final review of the code and tested several more times. Everything looks good, now need to prepare for final submisssion. 

### Session Goals

Final checks:
1. Review all output messages for correct formatting
2. Verify no compilation warnings
3. Test on cs1 machine to ensure compatibility
4. Create README file
5. Prepare for git repository creation

### Final Testing

Compiled and ran on cs1 machine - works perfectly, formatting is correct! 

Sample output verified. 

All interactions are occuring in the right order. 

### End of Session

Project is complete and ready for submission.
