package bravo.environment;

import java.io.File;
import java.util.Date;

/**
 * Classe que representa o diretório raiz do programa. Todos os arquivos em 
 * processamento serão gravados em subdiretórios do diretório raiz, bem como
 * arquivos necessários ao seu funcionamento. Nesta classe, será feito todo
 * o processo de criação de diretórios inexistentes na inicialização do programa,
 * bem como dá acesso a todos os subdiretórios essenciais.
 * 
 * @since 1.0
 */
public final class RootFolder {

    
    /**Diretório raiz do programa.*/
    private static final File rootFolder;
    
    /**Diretório de cache do programa.*/
    private static final File cacheFolder;
    
    /**Diretório de cache de sessão, aonde se extraem todos os arquivos no 
     * contexto do processamento do programa.*/
    private static final File sessionFolder;
    
    /**Subdiretório aonde extraem-se os arquivos abertos no programa.*/
    private static final File extractionFolder;
    
    /**Subdiretório aonde extraem-se os arquivos para troca da senha.*/
    private static final File changePasswordCacheFolder;

    /**Subdiretório aonde cria-se um arquivo vazio para cada extensão, afim de obter seu ícone.*/
    private static final File thumbnailsFolder;
    
    /**Subdiretório aonde serão gravados os arquivos criptografados pelo programa.*/
    private static final File encryptionFolder;
    
    
    //Cria todos os diretórios necessários.
    static {
        
        File path = null;
        
        try {
            path = new File(RootFolder.class.getProtectionDomain().getCodeSource()
            .getLocation().toURI()).getParentFile().getParentFile();
        } catch (Exception ex) {            
        }
        
        String rootPath = path.getAbsolutePath() + File.separator;
        String dateDigits = String.format("%1$td%1$tm%1$tY%1$tH%1$tM%1$tS", new Date());
        String cachePath = rootPath + "cache" + File.separator + dateDigits;
        
        // Pode ocorrer de o usuário clicar 2 vezes no ícone do programa antes
        // de o relógio mudar o segundo, neste caso, indexa se já existir o
        // diretório de cache com o mesmo nome, ficando, por exemplo:
        //
        // 26042024075245
        // 26042024075245-1
        // 26042024075245-2
        //
        // É uma situação improvável, mas caso venha a ocorrer, é necessário o
        // tratamento para o correto funcionamento do sistema. Este subdiretório
        // dentro do diretório cache é o cache de sessão, e quando fechar o
        // programa, todos os arquivos dentro dele serão apagados, podendo
        // interferir com outra instância se o diretório for compartilhado entre
        // ambas as sessões.
        
        int index = 1;
        while ((new File(cachePath)).exists()) {
            cachePath = rootPath + "cache" + File.separator + dateDigits + "-" +
            String.valueOf(index);
            index++;
        }
        
        rootFolder = new File(path.getAbsolutePath());
        cacheFolder = new File(rootPath + "cache");
        sessionFolder = new File(cachePath);
        encryptionFolder = new File(cachePath + File.separator  + "encrypted");
        thumbnailsFolder = new File(cachePath + File.separator + "thumbnails");
        extractionFolder = new File(cachePath + File.separator + "extracted");
        changePasswordCacheFolder = new File(cachePath + File.separator  + "extracted2");
        
        if (!sessionFolder.exists()) sessionFolder.mkdir(); 
        if (!encryptionFolder.exists()) encryptionFolder.mkdirs();               
        if (!thumbnailsFolder.exists()) thumbnailsFolder.mkdirs();
        if (!extractionFolder.exists()) extractionFolder.mkdirs();
        
    }
    
    
    /**
     * Obter o diretório de cache.
     * @return diretório.
     */
    public static File getCacheFolder() {
        return cacheFolder;
    }
    
    
    /**
     * Obter diretório de cache de sessão. O diretório de cache recebe um nome
     * distinto para cada instância em execução do programa, vizando que múltiplas
     * instâncias possam estar em execução sem conflitos. Dessa forma, pode-se 
     * trabalhar com múltiplos arquivos bravo abertos simultâneamente.
     * @return path do diretório cache.
     */
    public static File getSessionFolder() {
        return sessionFolder;
    }
    
    
    /**
     * Obter o diretório para extração dos arquivos que serão abertos na interface
     * do programa.
     * @return diretório.
     */
    public static File getExtractionFolder() {
        return extractionFolder;
    }
    
    
    /**
     * Obter o diretório para criação de um arquivo vazio para cada extensão,
     * afim de obter seu ícone no sistema.
     * @return diretório.
     */
    public static File getThumbnailsFolder() {
        return thumbnailsFolder;
    }
    
    
    /**
     * Obter o diretório aonde serão salvos os arquivos após a encriptação e 
     * antes da inserção no arquivo .bar.
     * @return diretório.
     */
    public static File getEncryptionFolder() {
        return encryptionFolder;
    }
    
    
    /**
     * Obter o diretório de extração dos arquivos para a troca da senha do arquivo
     * Bravo.
     * @return diretório.
     */
    public static File getChangePasswordCacheFolder() {
        if (!changePasswordCacheFolder.exists()) changePasswordCacheFolder.mkdirs();
        return changePasswordCacheFolder;
    }

    
    /**
     * Obter o diretório raiz.
     * @return diretório.
     */
    public static String getAbsolutePath() {
        return rootFolder.getAbsolutePath();
    }

    
}