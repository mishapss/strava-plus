import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

public class PasswordUtil {

    //Argon2id konfiguration (64MB Ram, 3 Iterationen, 1 Thread)
    public static final Argon2Function argon2 = Argon2Function.getInstance(
        65536, //Speicher (KB)
        3,  //Iterationen
        1,  //Parallelität
        32, //Hash-länge
        Argon2.ID
    );

    public static String hashPasswort(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("pass darf nicht null sein");
        }
        return Password.hash(plainPassword).with(argon2).getResult();
    }
    //Password.hash(plainPassword) nimmt passwort entgegen und generiert salt
    //with(argon2) wendet die konfiguration an
    //getResult() führt die rechnung aus und gibt das resultat (gehashte passwort) zurück 


    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$argon2id$")) {
            return false;
        }
        return Password.check(plainPassword, storedHash).with(argon2);
    }
    //Password.check(plainPassword, storedHash) vergleicht plainpassword mit dem, was in db gespeichert ist
    //with(argon2) hasht plainpassword mit exakt gleichen parametern und vergleicht gehashten password mit dem aus
}