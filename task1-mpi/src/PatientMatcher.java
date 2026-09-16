import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Task #1b - Simplified MPI matching function.
 */
public class PatientMatcher {

    record Patient(String name, String dob, String phone, String email) {}

    enum Decision { AUTO_MATCH, REVIEW, NO_MATCH }

    // ---------- Normalization ----------

    static String name(String s) {                        
        if (s == null) return null;
        String[] t = s.toLowerCase().replaceAll("[^a-z ]", " ").trim().split("\\s+");
        Arrays.sort(t);
        return String.join(" ", t);
    }

    private static final DateTimeFormatter[] DOB_FORMATS = {
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("d-M-yyyy"),
        DateTimeFormatter.ofPattern("yyyyMMdd")
    };

    static String dob(String s) {
        if (s == null) return null;
        String t = s.trim();
        for (DateTimeFormatter f : DOB_FORMATS) {
            try { return LocalDate.parse(t, f).toString(); }
            catch (DateTimeParseException ignored) { /* go next */ }
        }
        return null;
    }

    static String phone(String s) {
        if (s == null) return null;
        String d = s.replaceAll("\\D", "");
        return d.startsWith("61") ? "0" + d.substring(2) : d;
    }

    static String email(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    // ---------- matching ----------

    static Decision match(Patient x, Patient y) {
        String[] a = { name(x.name()), dob(x.dob()), phone(x.phone()), email(x.email()) };
        String[] b = { name(y.name()), dob(y.dob()), phone(y.phone()), email(y.email()) };

        int agree = 0, conflict = 0;
        for (int i = 0; i < 4; i++) {
            if (a[i] == null || b[i] == null) continue;   // missing data is not a conflict
            if (a[i].equals(b[i])) agree++; else conflict++;
        }

        if (agree >= 2 && conflict == 0) return Decision.AUTO_MATCH;
        if (agree >= 2 || (agree == 1 && conflict <= 1)) return Decision.REVIEW;
        return Decision.NO_MATCH;
    }

    // ---------- sample ----------

    public static void main(String[] args) {
        Patient existing = new Patient("Ali Rahman", "1990-01-01", "+61 412 345 678", "Ali.Rahman@mail.com");

        // S1: same person
        Patient s1 = new Patient("ALI RAHMAN", "1990-01-01", "0412-345-678", "ali.rahman@MAIL.com");
        // S2: name + dob match, but phone and email different
        Patient s2 = new Patient("Rahman, Ali", "1990-01-01", "0499 111 222", "ali.r@outlook.com");
        // S3: Different person
        Patient s3 = new Patient("Budi Rahman", "1985-07-23", "0433 000 111", "budi.r@outlook.com");
        // S4: same person, date of birth sent in a different format
        Patient s4 = new Patient("ALI RAHMAN", "01/01/1990", "0412-345-678", "ali.rahman@MAIL.com");
        // S5: same person, different date format, wrong email
        Patient s5 = new Patient("ALI RAHMAN", "01/01/1990", "0412-345-678", "ali. rahman@MAIL.com");

        System.out.println("Sample 1 -> " + match(s1, existing));
        System.out.println("Sample 2 -> " + match(s2, existing));
        System.out.println("Sample 3 -> " + match(s3, existing));
        System.out.println("Sample 4 -> " + match(s4, existing));
        System.out.println("Sample 5 -> " + match(s5, existing));
    }
}
