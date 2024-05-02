package bravo.file;

/**
 * Modo de operação com o arquivo.
 * 
 * @since 1.0
 */
public enum FileOperation {
    
    /**Adicionar arquivo ao ZIP.*/
    ADD,
    
    /**Remover arquivo do ZIP.*/
    REMOVE,
    
    /**Destruir arquivo.*/
    WIPE,
    
    /**Extrair arquivo do ZIP.*/
    EXTRACT,
    
    /**Encriptar arquivo.*/
    ENCRYPT; 
    
}