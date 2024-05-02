package bravo.file;

/**
 * Ouvinte de processo de encriptação/decriptação de arquivos.
 * 
 * @since 1.0
 */
public interface CipherListener {
    
    
    /**
     * Atualizar o número de bytes processados na etapa.
     * @param numberOfBytes número de bytes processados.
     */
    public void update(long numberOfBytes);
    
    
    /**
     * Abortar o processo de encriptação/decriptação de arquivos.
     * @return true, se o usuário decidiu por abortar o processo de 
     * encriptação/decriptação, false, se o processo deve ser continuado.
     */
    public boolean abort();
    
    
}