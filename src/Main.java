public class Main {
    public static void main(String[] args) {
        System.out.println("[FISCSAS] Assembling program test.s");
        Fiscas as = new Fiscas("test.s");
        System.out.println("[FISCSIM] Loading object file test.hex");
        Fiscsim fs = new Fiscsim("test.hex");
    }
}
