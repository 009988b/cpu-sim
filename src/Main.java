public class Main {
    public static void main(String[] args) {
        System.out.println("[FISCAS] Assembling program");
        Fiscas as = new Fiscas("fibo2.s");
        System.out.println("[FISCSIM] Loading object file");
        Fiscsim fs = new Fiscsim("test.hex");
    }
}
