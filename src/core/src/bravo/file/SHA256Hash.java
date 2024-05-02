package bravo.file;

import java.util.Arrays;
import org.bouncycastle.crypto.digests.SHA256Digest;

final class SHA256Hash {
    
    
    public byte[] getBytes(byte[] input) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(input, 0, input.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }
    
    
    public byte[] getBytes(byte[] input, byte[] salt) {
        byte[] dataBytes = Arrays.copyOf(input, input.length + salt.length);
        System.arraycopy(salt, 0, dataBytes, input.length, salt.length);
        SHA256Digest digest = new SHA256Digest();
        digest.update(dataBytes, 0, dataBytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }
    
    
}