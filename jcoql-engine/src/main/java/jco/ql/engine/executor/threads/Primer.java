package jco.ql.engine.executor.threads;

/*
 * Return the next prime-like number that follows the parameter. 
 * Prime-like number is intended to be the first odd number that cannot divided by prime numbers up to 101
 * There are 4 implementations: 2 are recursive and 2 are iterative
 */
public class Primer {

	static final int[] primes = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101};

	static public int getNextRecursively (int prime) {
    	if (prime % 2 == 0)
    		return getNextRecursively (prime+1);

    	for (int i=0; i<primes.length; i++)
    		if (prime % primes[i] == 0)
	    		return getNextRecursively (prime+2);

    	return prime;
    }

    
    static public int getNextIteratively (int prime) {
    	int p = prime;

    	while (p == prime) {
	    	if (prime % 2 == 0)
	    		prime++;
	    	for (int i=0; i<primes.length; i++)
	    		if (prime % primes[i] == 0)
	    			prime += 2;

	    	if (p != prime)
	    		p = prime;
	    	else
	    		p = 0;
    	}

    	return prime;
    }

    
    static public int getNextRecursivelyFast (int prime) {
    	if (prime % 2 == 0)
    		return getNextRecursivelyFast (prime+1);
    	if (prime % 3 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 5 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 7 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 11 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 13 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 17 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 19 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 23 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 29 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 31 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 37 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 41 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 43 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 47 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 53 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 59 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 61 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 67 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 71 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 73 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 79 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 83 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 89 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 97 == 0)
    		return getNextRecursivelyFast (prime+2);
    	if (prime % 101 == 0)
    		return getNextRecursivelyFast (prime+2);

    	return prime;
    }

    
    static public int getNextIterativelyFast (int prime) {
    	int p = prime;
    	while (p == prime) {
	    	if (prime % 2 == 0)
	    		prime++;
	    	if (prime % 3 == 0)
	    		prime+=2;
			if (prime % 5 == 0)
	    		prime+=2;
	    	if (prime % 7 == 0)
	    		prime+=2;
	    	if (prime % 11 == 0)
	    		prime+=2;
	    	if (prime % 13 == 0)
	    		prime+=2;
	    	if (prime % 17 == 0)
	    		prime+=2;
	    	if (prime % 19 == 0)
	    		prime+=2;
	    	if (prime % 23 == 0)
	    		prime+=2;
	    	if (prime % 29 == 0)
	    		prime+=2;
	    	if (prime % 31 == 0)
	    		prime+=2;
	    	if (prime % 37 == 0)
	    		prime+=2;
	    	if (prime % 41 == 0)
	    		prime+=2;
	    	if (prime % 43 == 0)
	    		prime+=2;
	    	if (prime % 47 == 0)
	    		prime+=2;
	    	if (prime % 53 == 0)
	    		prime+=2;
	    	if (prime % 59 == 0)
	    		prime+=2;
	    	if (prime % 61 == 0)
	    		prime+=2;
	    	if (prime % 67 == 0)
	    		prime+=2;
	    	if (prime % 71 == 0)
	    		prime+=2;
	    	if (prime % 73 == 0)
	    		prime+=2;
	    	if (prime % 79 == 0)
	    		prime+=2;
	    	if (prime % 83 == 0)
	    		prime+=2;
	    	if (prime % 89 == 0)
	    		prime+=2;
	    	if (prime % 97 == 0)
	    		prime+=2;
	    	if (prime % 101 == 0)
	    		prime+=2;

	    	if (p != prime)
	    		p = prime;
	    	else
	    		p=0;
    	}
    	return prime;
    }

}
