import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;

public class Fiscsim {
    private int PC;
    private int clk;

    private boolean Z;

    private byte[] registers;

    private ArrayList<Integer> memory; //Instruction memory

    public Fiscsim(String objFileName) {
        registers = new byte[4];
        for (int i = 0; i < 4; i++)
            registers[i] = 0;
        memory = new ArrayList<>();
        Z = false;
        PC = 0;
        loadProgram(objFileName);
        System.out.println("[FISCSIM] Loading object file "+objFileName);
        exec();
    }

    private void debugState() {
        System.err.println("[STATE]\t\t\tclk: "+clk+"\tPC: 0"+PC+"\tZ: "+Z+"\t\tR0: "+Integer.toHexString(registers[0])+"\t\tR1: "+Integer.toHexString(registers[1])+"\tR2: "+Integer.toHexString(registers[2])+"\tR3: "+Integer.toHexString(registers[3]));
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
        }
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
            //ADD: Rd <- Rn + Rm, Z <- (Rd == 0)
            registers[Rd] = (byte) (registers[Rn] + registers[Rm]);
            debugState();
            System.err.println("[disassembly]\tadd r"+Rd+" r"+Rn+" r"+Rm+"\tnew value: "+registers[Rd]);
            if (Rd == 0) {
                Z = true;
            } else {
                Z = false;
            }
        }
        if (str.startsWith("01")) {
            //AND: Rd <- Rn * Rm, Z <- (Rd == 0), PC <- PC+1
            registers[Rd] = (byte) (registers[Rn] & registers[Rm]);
            debugState();
            System.err.println("[disassembly]\tand r"+Rd+" r"+Rn+" r"+Rm+"\tnew value: "+registers[Rd]);
            if (Rd == 0) {
                Z = true;
            } else {
                Z = false;
            }
        }
        if (str.startsWith("10")) {
            //NOT: Rd <- !Rn, Z <- (Rd == 0), PC <- PC+1
            String s = to8BitString(registers[Rn]);
            BitSet bs = new BitSet();
            for (int bitIdx = s.length()-1; bitIdx >= 0; bitIdx--) {
                bs.set(bitIdx, s.charAt(bitIdx) == '1');
            }
            bs.flip(1,8);
            String flipped = "";
            for (int x = 8; x >= 1; x--) {
                flipped += (bs.get(x)? 1 : 0);
            }
            registers[Rd] = (byte) Integer.parseInt(flipped,2);
            debugState();
            System.err.println("[disassembly]\tnot r"+Rd+" r"+Rn+"\t\tnew value: "+Integer.parseInt(flipped,2));
            if (Rd == 0) {
                Z = true;
            } else {
                Z = false;
            }
        }
        if (str.startsWith("11")) {
            //BNZ: if (!Z) PC <- target
            int target = Integer.parseInt(str.substring(2,str.length()),2);
            if (!Z) {
                PC = target;
                System.err.println("[disassembly]\tbnz target addr:\t 0x"+Integer.toHexString(target));
            } else {
                System.err.println("[disassembly]\tbnz cannot branch - zero flag set ");
            }
            debugState();
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
                    memory.add(0b00+Integer.parseInt(line,16));
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
