package bravo.file;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Gerador de Hash baseado no algoritmo Argon2. Este algoritmo foi criado por
 * Alex Biryukov e Dmitry Khovratovich, ambos membros da Universidade de Luxemburgo
 * e foi vencedor da competição Password Hashing Competition (PHC) em 2015. Ele
 * possui várias características distintas que contribuem para sua resistência
 * e eficácia. 
 * 
 * <br><br>
 * 
 * Em primeiro lugar, o Argon2id é projetado para ser resistente a 
 * ataques de força bruta e de dicionário, dois dos métodos mais comuns utilizados
 * para decifrar senhas. Ele alcança isso através do uso de um alto custo 
 * computacional e de memória.
 * 
 * <br><br>
 * 
 * Além disso, o Argon2id é resistente a ataques paralelos devido à sua estrutura
 * de memória interconectada. Isso significa que ele é eficaz mesmo contra atacantes
 * que possuem recursos de hardware substanciais.
 * 
 * <br><br>
 * 
 * A implementação é feita pela biblioteca BouncyCastle, disponível em
 * <a href="https://www.bouncycastle.org">https://www.bouncycastle.org</a>.
 * 
 * @since 2.0
 */
final class Argon2Hash {
    
    
    /**
     * Obter o hash a partir dos bytes de entrada.
     * @param input bytes de entrada.
     * @param salt bytes do salt.
     * @param iterations número de iterações.
     * @param memory memória alocada.
     * @param parallelism número de threads.
     * @param outputLength tamanho do hash de saída.
     * @return hash dos bytes de entrada.
     */
    public byte[] getBytes(byte[] input, byte[] salt, int iterations,
    int memory, int parallelism, int outputLength) {
        
        Argon2Parameters.Builder builder = new Argon2Parameters
        .Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(iterations)
        .withMemoryAsKB(memory)
        .withParallelism(parallelism)
        .withSalt(salt);
        
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        
        byte[] result = new byte[outputLength];
        
        generator.generateBytes(input,
            result,
            0,
            result.length
        );
        
        return result;
        
    }
    
    
    /**
     * Obter o hash da senha.
     * @param password senha.
     * @param salt bytes do salt.
     * @param iterations número de iterações.
     * @param memory memória alocada.
     * @param parallelism número de threads.
     * @param outputLength tamanho do hash de saída.
     * @return hash da senha.
     */
    public byte[] getBytes(char[] password, byte[] salt, int iterations,
    int memory, int parallelism, int outputLength) {
        byte[] bytes = new byte[password.length];
        for(int i= 0; i < password.length; i++) {
            bytes[i] = (byte)(0xFF & (int)password[i]);
        }
        return getBytes(
            bytes,
            salt,
            iterations,
            memory,
            parallelism,
            outputLength
        );
    }
    
    
}