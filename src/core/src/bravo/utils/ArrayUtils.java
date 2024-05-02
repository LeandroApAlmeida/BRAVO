package bravo.utils;

import java.nio.ByteBuffer;

/**
 * Classe para manipulação de arrays de objetos.
 * 
 * @since 1.0
 */
public class ArrayUtils {
    
    
    /**
     * Converter um integer em um array de bytes correspondente.
     * @param i integer a ser convertido para array de bytes.
     * @return array de bytes correspondente ao integer.
     */
    public static byte[] intToByteArray(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        return buffer.array();
    }

    
    /**
     * Converter um array de bytes em um integer correspondente.
     * @param bytes array de bytes a ser convertido para integer.
     * @return integer correspondente ao array de bytes.
     */
    public static int byteArrayToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }
  
    
}
