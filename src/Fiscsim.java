import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;

public class Fiscsim {
    private int PC;                          // Program Counter
    private int clk;                         // Current cycle

    private boolean Z;                       // Zero flag

    private final int[] registers;           // R0 - R3

    private final ArrayList<Integer> memory; // Instruction Memory

    private final boolean d;                 // Debug state flag
    private final int cycles;                // Max clock cycles

    public Fiscsim(String objFileName, boolean debugState, int cycles) {
        d = debugState;
        this.cycles = cycles;
        registers = new int[4];
        for (int i = 0; i < 4; i++)
            registers[i] = 0;
        memory = new ArrayList<>();
        Z = false;
        PC = 0;
        loadProgram(objFileName);
        exec();
    }

    private void debugState() {
        String s = "[STATE]\t\t\tclk: "+clk+"\tPC: 0"+PC+"\tZ: "+Z+"\t\tR0: ";
        s += Integer.toHexString(registers[0])+"\t\tR1: ";
        s += Integer.toHexString(registers[1]);
        s +="\tR2: "+Integer.toHexString(registers[2])+"\tR3: ";
        s +=Integer.toHexString(registers[3]);
        System.out.println(s);
    }

    private void setZeroFlag(int Rd) {
        Z = registers[Rd] == 0;
    }

    private static String to8BitString(int n) {
        String bin = Integer.toBinaryString(n);
        int numZeros = 8-bin.length();
        String str = "";
        for (int x = 0; x < numZeros; x++) {
            str += "0";
        }
        return str+bin;
    }

    private void exec() {
        while (PC < memory.size()) {
            performAction(memory.get(PC));
            PC++;
            clk++;
            if (clk > cycles) System.exit(0);
        }
    }

    private void add(int Rd, int Rn, int Rm) {
        //ADD: Rd <- Rn + Rm, Z <- (Rd == 0)
        registers[Rd] = (registers[Rn] + registers[Rm]);
        if (registers[Rd] >= 256) {
            // 'Overflow'
            registers[Rd] -= 256;
        }
        setZeroFlag(Rd);
        if (d) {
            debugState();
            String s = "[disassembly]\tadd r"+Rd+" r"+Rn+" r"+Rm;
            s += "\tnew value: "+registers[Rd];
            System.out.println(s);
        }
    }

    private void and(int Rd, int Rn, int Rm) {
        //AND: Rd <- Rn * Rm, Z <- (Rd == 0), PC <- PC+1
        registers[Rd] = (registers[Rn] & registers[Rm]);
        setZeroFlag(Rd);
        if (d) {
            debugState();
            String s = "[disassembly]\tand r"+Rd+" r"+Rn+" r"+Rm;
            s += "\tnew value: "+Integer.toHexString(registers[Rd]);
            System.out.println(s);
        }
    }

    private void not(int Rd, int Rn) {
        //NOT: Rd <- !Rn, Z <- (Rd == 0), PC <- PC+1
        String s = to8BitString(registers[Rn]);
        BitSet bs = new BitSet();
        for (int bitIdx = s.length()-1; bitIdx > -1; bitIdx--) {
            bs.set(bitIdx, s.charAt(bitIdx) == '0'); //flip
        }
        StringBuilder flipped = new StringBuilder();
        for (int x = 0; x < s.length(); x++) {
            boolean z = bs.get(x);
            flipped.append(z ? 1 : 0);
        }
        registers[Rd] = Integer.parseInt(flipped.toString(),2);
        setZeroFlag(Rd);
        if (d) {
            debugState();
            String str = "[disassembly]\tnot r" + Rd + " r" + Rn;
            str += "\t\tnew value: " + Integer.parseInt(flipped.toString(), 2);
            System.out.println(str);
        }
    }

    private void bnz(String str) {
        //BNZ: if (!Z) PC <- target
        int target = Integer.parseInt(str.substring(2),2);
        if (d) debugState();
        if (!Z) {
            PC = target-1;
            String s = Integer.toHexString(target);
            if (d) System.out.println("[disassembly]\tbnz target addr:\t 0x"+s);
        } else {
            if (d) System.out.println("[disassembly]\tbnz cannot branch - Z is set");
        }
    }

    private void performAction(int instruction) {
        //Fetch
        String str = to8BitString(instruction);
        //Decode & Execute
        int opCode = Integer.parseInt(str.substring(0,2),2);
        int Rd = Integer.parseInt(str.substring(2,4),2);
        int Rn = Integer.parseInt(str.substring(4,6),2);
        int Rm = Integer.parseInt(str.substring(6,8),2);
        switch (opCode) {
            case 0b00:
                add(Rd, Rn, Rm);
                break;
            case 0b01:
                and(Rd, Rn, Rm);
                break;
            case 0b10:
                not(Rd, Rn);
                break;
            case 0b11:
                bnz(str);
                break;
        }
    }

    private void loadProgram(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            int idx = 0;
            while (line != null) {
                if (idx > 0) {
                    memory.add(Integer.parseInt(line,16));
                }
                line = reader.readLine();
                idx++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filename = "";
        boolean d = false;
        int cycles = 20;
        for (String s : args) {
            if (s.contains(".hex")) {
                filename = s;
            }
            if (s.contains("-d")) {
                d = true;
            }
            try {
                if (Integer.parseInt(s) > 0) {
                    cycles = Integer.parseInt(s);
                }
            } catch (Exception e) {
                // Not all arguments should be numbers
            }
        }
        System.out.println("[FISCSIM] Loading object file "+filename);
        new Fiscsim(filename,d,cycles);
    }
}