package bravo.file;

/**
 * Parâmetros para o algoritmo Argon2.
 * 
 * @since 2.0
 */
public final class Argon2Params {
    
    
    /**Número de iterações.*/
    private final int iterations;
    
    /**Memória alocada.*/
    private final int memory;
    
    /**Número de Threads.*/
    private final int parallelism;

    
    /**
     * Constructor padrão.
     * @param iterations número de iterações.
     * @param memory memória alocada.
     * @param parallelism número de Threads.
     */
    public Argon2Params(int iterations, int memory, int parallelism) {
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    
    /**
     * Obter o número de iterações.
     * @return número de iterações.
     */
    public int getIterations() {
        return iterations;
    }

    
    /**
     * Obter a memória alocada.
     * @return memória alocada.
     */
    public int getMemory() {
        return memory;
    }

    
    /**
     * Obter o número de Threads.
     * @return número de Threads.
     */
    public int getParallelism() {
        return parallelism;
    }
 
    
}