package de.codehat.mcwrapper;

/**
 * MCWrapper main class.
 */
public class Main {

    /**
     * Main method.
     *
     * @param args Application arguments.
     */
    public static void main(String[] args) {
        // Rename the Thread to 'Main'
        Thread.currentThread().setName("Main");

        // Start a new MCWrapper instance
        MCWrapper mcWrapper = new MCWrapper(args);
    }

}
