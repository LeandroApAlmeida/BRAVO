package bravo.environment;

import java.util.prefs.Preferences;

/**
 * Classe para ler e gravar as configurações do programa. Em ambiente Microsoft
 * Windows elas ficam no registro, na chave HKEY_CURRENT_USER/Software/JavaSoft/bravo-cipher.
 * Em ambientes Linux, ficam em /home/.../.java/.userPrefs. 
 * 
 * @since 1.0
 */
public final class Config {

    
    /**Instância da Api Preferences do Java.*/
    private static Preferences prefs = Preferences.userRoot().node("/bravo-cipher");

    
    /**
     * Escrever um valor string.
     * @param key chave
     * @param value valor string a escrever. 
     */
    public static void putString(String key, String value) {
        prefs.put(key, value); 
    }
    
    
    /**
     * Escrever um valor integer.
     * @param key chave
     * @param value valor integer a escrever.
     */
    public static void putInt(String key, int value) {
        prefs.putInt(key, value); 
    }
    
    
    /**
     * Escrever um valor long.
     * @param key chave
     * @param value valor long a escrever.
     */
    public static void putLong(String key, long value) {
        prefs.putLong(key, value); 
    }
    
    
    /**
     * Escrever um valor float.
     * @param key chave
     * @param value valor float a escrever. 
     */
    public static void putFloat(String key, float value) {
        prefs.putFloat(key, value); 
    }
    
    
    /**
     * Escrever um valor double.
     * @param key chave
     * @param value valor double a escrever. 
     */
    public static void putDouble(String key, double value) {
        prefs.putDouble(key, value); 
    }
    
    
    /**
     * Escrever um valor boolean.
     * @param key chave
     * @param value valor boolean a escrever. 
     */
    public static void putBoolean(String key, boolean value) {
        prefs.putBoolean(key, value); 
    }

    
    /**
     * Escrever um valor byte array.
     * @param key chave
     * @param value valor byte array a escrever. 
     */
    public static void putByteArray(String key, byte[] value) {
        prefs.putByteArray(key, value);
    }

    
    /**
     * Ler um valor string.
     * @param key chave
     * @param defaultValue valor padrão
     * @return valor string lido.
     */
    public static String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }
    
    
    /**
     * Ler um valor inteiro.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor inteiro lido.
     */
    public static int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    
    /**
     * Ler um valor long.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor long lido.
     */
    public static long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }
    
    
    /**
     * Ler um valor float.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor float lido.
     */
    public static float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }
    
    
    /**
     * Ler um valor double.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor double lido.
     */
    public static double getDouble(String key, double defaultValue) {
        return prefs.getDouble(key, defaultValue);
    }
    
    
    /**
     * Ler um valor boolean.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor boolean lido.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    
    /**
     * Ler um valor byte array.
     * @param key chave
     * @param defaultValue valor padrão.
     * @return valor byte array lido.
     */
    public static byte[] getByteArray(String key, byte[] defaultValue) {
        return prefs.getByteArray(key, defaultValue);
    }

    
}