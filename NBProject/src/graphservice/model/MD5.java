import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {
    public static int md5(String numServidores) throws Exception{
       String s="Texto de Exemplo";
       MessageDigest m=MessageDigest.getInstance("MD5");
       m.update(s.getBytes(),0,s.length());
       return (new BigInteger(1,m.digest()).mod(new BigInteger(numServidores))).intValue();
    }
}   