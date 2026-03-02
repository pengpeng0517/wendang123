import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin123";
        String warehousePassword = "warehouse123";
        
        String adminHash = encoder.encode(adminPassword);
        String warehouseHash = encoder.encode(warehousePassword);
        
        System.out.println("admin123 hash: " + adminHash);
        System.out.println("warehouse123 hash: " + warehouseHash);
        
        System.out.println("\nAdmin hash length: " + adminHash.length());
        System.out.println("Warehouse hash length: " + warehouseHash.length());
    }
}