package bravo.environment;


/**
 * Método de sobrescrita de arquivos em disco.
 * 
 * @since 1.0
 */
public enum WipeMethod {
    
    /**
     * Este método usa a sobrescrita com um byte específico (exemplo: 0x00).
     */
    FIXED_BYTE,
    
    /**
     * Este método usa a sobrescrita com bytes gerados de modo pseudo-aleatório.
     */
    RANDOM_BYTES,
    
    /**
     * Este método foi desenvolvido em 1996 por Peter Gutmann. Ele funciona 
     * substituindo alguns valores pseudo-aleatórios 35 vezes com 35 passagens
     * ou ciclos de substituição. O algoritmo de Gutmann usa valores aleatórios
     * para as primeiras e últimas 4 passagens e emprega um padrão complexo nas
     * passagens que ficam na faixa de 5 a 31. É um dos métodos de eliminação de
     * dados mais eficazes, embora consuma muito tempo para a sua execução.
     */
    GUTMANN_ALGORITHM,
    
    /**
     * Este método foi desenvolvido pelo Escritório Federal Alemão de Segurança 
     * de TI no ano 2000. Ele consiste em sete passos ou ciclos de substituição.
     * Cada limpeza, nas primeiras 6 passagens, inverte o padrão de bits da 
     * limpeza anterior. A 7ª passagem final amplifica o efeito de substituição.
     * O algoritmo VSITR é considerado um método seguro de apagamento de dados,
     * mas requer um tempo considerável para a sua execução.
     */
    VSITR_ALGORITHM,
    
    /**
     * Este método foi desenvolvido por Bruce Schneier em 1994. O processo 
     * consiste em 7 passos em que a substituição é feita com 0's e 1's nos
     * primeiros 2 passos e sucedido por 5 passos aleatórios. O algoritmo de 
     * Bruce Schneier é considerado um dos métodos mais seguros e confiáveis 
     * ​​para exclusão de dados.
     */
    BRUCE_SCHNEIER_ALGORITHM,
    
    /**
     * Este método foi desenvolvido pelo departamento de Defesa dos EUA em 1995.
     * O padrão define a implementação de três passagens de substituição seguras
     * com verificação ao final de cada passagem. A passagem 1 envolve a substituição
     * com zeros binários, a passagem 2 com uns binários e a passagem 3 com um 
     * padrão de bits aleatório.
     * 
     * <br><br>
     * 
     * Em 2001 o departamento publicou o DoD 5220.22-M ECE, que consiste
     * em 7 passos ou ciclos de substituição. Ele executa o DoD 5220.22-M duas 
     * vezes e uma passagem extra (DoD 5220.22-M (C) Standard) entre elas.
     */
    DOD522022M_ALGORITHM;
    
}