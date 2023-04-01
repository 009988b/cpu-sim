import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;

public class Fiscsim {
    private int PC;
    private int clk;

    private boolean Z;

    private int[] registers;

    private ArrayList<Integer> memory; //Instruction memory

    public Fiscsim(String objFileName) {
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
        if (registers[Rd] == 0) {
            Z = true;
        } else {
            Z = false;
        }
    }

    private static String to8BitString(int n) {
        String bin = Integer.toBinaryString(n);
        int numZeros = 8-bin.length();
        String zeros = "";
        for (int z = 0; z < numZeros; z++) {
            zeros += "0";
        }
        bin = zeros + bin;
        return bin;
    }

    private void exec() {
        while (PC < memory.size()) {
            performAction(memory.get(PC));
            PC++;
            clk++;
            if (clk > 1100) System.exit(0);
        }
    }

    private void add(int Rd, int Rn, int Rm) {
        //ADD: Rd <- Rn + Rm, Z <- (Rd == 0)
        registers[Rd] = (registers[Rn] + registers[Rm]);
        if (registers[Rd] > 256) {
            registers[Rd] -= 256;
        }
        setZeroFlag(Rd);
        debugState();
        String s = "[disassembly]\tadd r"+Rd+" r"+Rn+" r"+Rm;
        s += "\tnew value: "+registers[Rd];
        System.out.println(s);
    }

    private void and(int Rd, int Rn, int Rm) {
        //AND: Rd <- Rn * Rm, Z <- (Rd == 0), PC <- PC+1
        registers[Rd] = (registers[Rn] & registers[Rm]);
        setZeroFlag(Rd);
        debugState();
        String s = "[disassembly]\tand r"+Rd+" r"+Rn+" r"+Rm;
        s += "\tnew value: "+registers[Rd];
        System.out.println(s);
    }

    private void not(int Rd, int Rn) {
        //NOT: Rd <- !Rn, Z <- (Rd == 0), PC <- PC+1
        String s = to8BitString(registers[Rn]);
        BitSet bs = new BitSet();
        for (int bitIdx = s.length()-1; bitIdx > -1; bitIdx--) {
            bs.set(bitIdx, s.charAt(bitIdx) == '0'); //flip
        }
        String flipped = "";
        for (int x = 0; x < s.length(); x++) {
            boolean z = bs.get(x);
            flipped += (z? 1 : 0);
        }
        registers[Rd] = Integer.parseInt(flipped,2);
        setZeroFlag(Rd);
        debugState();
        String d = "[disassembly]\tnot r"+Rd+" r"+Rn;
        d += "\t\tnew value: "+Integer.parseInt(flipped,2);
        System.out.println(d);
    }

    private void bnz(String str) {
        //BNZ: if (!Z) PC <- target
        int target = Integer.parseInt(str.substring(2),2);
        if (!Z) {
            PC = target;
            String s = Integer.toHexString(target);
            System.out.println("[disassembly]\tbnz target addr:\t 0x"+s);
        } else {
            System.out.println("[disassembly]\tbnz cannot branch - Z is set");
        }
        debugState();
    }

    private void performAction(int instruction) {
        //Fetch
        String str = to8BitString(instruction);
        //System.out.println(bin);
        //Decode & Execute
        int Rd = Integer.parseInt(str.substring(2,4),2);
        int Rn = Integer.parseInt(str.substring(4,6),2);
        int Rm = Integer.parseInt(str.substring(6,8),2);
        if (str.startsWith("00")) {
            add(Rd,Rn,Rm);
        }
        if (str.startsWith("01")) {
            and(Rd,Rn,Rm);
        }
        if (str.startsWith("10")) {
            not(Rd,Rn);
        }
        if (str.startsWith("11")) {
            bnz(str);
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
            System.exit(-1);
        }
    }
}
