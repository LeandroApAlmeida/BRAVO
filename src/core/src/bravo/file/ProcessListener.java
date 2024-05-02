package bravo.file;

/**
 * Ouvinte de processamento de arquivo. Sua finalidade é a de ser notificado a cada
 * etapa do processamento de um arquivo como adição em um ZIP, encriptação do arquivo,
 * remoção do ZIP, etc.
 * 
 * @since 1.0
 */
public interface ProcessListener {
    
    
    /**
     * Atualiza o arquivo atual em processamento.
     * @param file arquivo sendo processado.
     * @param operation modo de operação que identifica o tipo de processo
     * que está sendo realizado com o arquivo.
     */
    public void updateFile(String file, FileOperation operation);
    
    
    /**
     * Atualizar a percentagem total concluída do processo em andamento.
     * @param percentage percentagem do processo em andamento.
     */
    public void updateTotalPercentage(int percentage);
    
    
    /**
     * Atualizar a percentagem total concluída do processamento do arquivo na
     * etapa.
     * @param percentage percentagem do processamento do arquivo.
     */
    public void updateFilePercentage(int percentage);
    
    
    /**
     * Sinalização de que o processamento dos arquivos foi concluído.
     */
    public void done();
    
    
    /**
     * Controle de abortamento do processo. Se o usuário decidir pelo abortamento,
     * configurar o retorno como true, e imediatamente após a leitura será
     * cancelada a operação corrente.
     * @return se true, aborta a operação corrente, se false, continua o processamento
     * dos arquivos.
     */
    public boolean abort();
    
    
    public void abortBlocked(boolean status);
    
    
}