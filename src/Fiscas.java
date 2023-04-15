import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class Fiscas {
    private final ArrayList<Integer> instructionSet;
    private final ArrayList<String> lines;
    private final HashMap<String, Integer> labels;

    private int cursorOffset = 0;

    public Fiscas(String sourceFileName, boolean printTables) {
        instructionSet = new ArrayList<>();
        lines = new ArrayList<>();
        labels = new HashMap<>();
        readFile(sourceFileName);
        writeFile();
        if (printTables) {
            printTables();
        }
    }

    private String encodeRegister(String r) throws Exception {
        return switch (r) {
            case "r0", "" -> "00";
            case "r1" -> "01";
            case "r2" -> "10";
            case "r3" -> "11";
            default ->
                    throw new Exception("[PARSING ERROR] Invalid register: " + r);
        };
    }

    private int parseCommand(String cmd) {
        String result;
        try {
            cmd = cmd.trim();
            if (cmd.contains(",")) {
                String msg = "[PARSING ERROR] Invalid assembler line: "+cmd;
                msg += "\t - Expecting space separation";
                throw new Exception(msg);
            }
            String[] c = cmd.split(" ");
            String i;
            String rd = "";
            String rn = "";
            String rm = "";
            if (cmd.startsWith(";")) return -1;
            if (!cmd.contains(":")) {
                i = c[0];
                rd = c[1];
                if (c.length > 2) {
                    rn = c[2];
                }
                if (c.length > 3) {
                    rm = c[3];
                }
            } else {
                i = c[2];
                if (c.length > 3) {
                    rd = c[3];
                }
                if (c.length > 4) {
                    rn = c[4];
                }
                if (c.length > 5) {
                    rm = c[5];
                }
            }

            switch (i) {
                case "add" -> {
                    result = "00";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += encodeRegister(rm);
                }
                case "and" -> {
                    result = "01";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += encodeRegister(rm);
                }
                //return Integer.parseInt(result,2);

                case "not" -> {
                    result = "10";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += encodeRegister(rm);
                }
                case "bnz" -> {
                    result = "11";
                    if (labels.get(c[1]) == null) {
                        String msg = "[PARSING ERROR] Undefined label: " + c[1];
                        throw new Exception(msg);
                    }
                    String target = Integer.toBinaryString(
                            labels.get(c[1])
                                    - cursorOffset);
                    int l = 6 - target.length();
                    StringBuilder rb = new StringBuilder(result);
                    rb.append("0".repeat(Math.max(0, l)));
                    result = rb.toString();
                    int s = lines.size();
                    if (Integer.parseInt(target, 2) > s) {
                        String msg = "[PARSING ERROR] Target address out";
                        msg += "of bounds: 0b" + target + " max: " + s;
                        throw new Exception(msg);
                    }
                    result += target;
                }
                default -> {
                    String msg = "[PARSING ERROR] Instruction {" + i + "} not ";
                    msg += "recognized in set {add, and, not, bnz}";
                    throw new Exception(msg);
                }
            }
            return Integer.parseInt(result, 2);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        return -1;
    }

    private void readFile(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            int idx = 0;
            // 1ST PASS
            while (line != null) {
                // gathering labels
                if (line.contains(":")) {
                    String[] s = line.split(":");
                    String label = s[0];
                    if (labels.containsKey(label)) {
                        String msg = "[ERROR] Label <"+label+"> on line <";
                        msg += idx+"> is already defined";
                        throw new Exception(msg);
                    }
                    labels.put(label, idx-cursorOffset);
                }
                if (!line.isBlank() && !line.trim().startsWith(";"))
                    lines.add(line);
                else
                    cursorOffset+=1;
                line = reader.readLine();
                idx++;
            }
            reader.close();
            // 2ND PASS
            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            idx = 0b0;
            while (line != null) {
                String cmd;
                if (line.trim().startsWith(";") || line.isBlank()) {
                    // Ignore comment only lines and blank lines
                    line = reader.readLine();
                    idx-=1;
                    continue;
                }
                if (line.contains(";")) {
                    String[] c = line.split(";");
                    if (!line.contains(":")) {
                        cmd = c[0];
                    } else {
                        cmd = c[0].split(":")[1];
                    }
                } else {
                    if (line.contains(":")) {
                        cmd = line.split(":")[1];
                    } else {
                        cmd = line;
                    }
                }
                // translate to machine code
                if (!cmd.equals("")) instructionSet.add(parseCommand(cmd));
                line = reader.readLine();
                idx++;
            }
            reader.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void printTables() {
        System.err.println("*** LABEL LIST ***");
        labels.forEach((k,v) -> System.err.println(k+"\t"+(v)));
        System.err.println("*** MACHINE PROGRAM ***");
        instructionSet.forEach((i) -> {
            int idx = instructionSet.indexOf(i);
            String d = "0"+idx+":"+Integer.toHexString(i);
            d += "\t"+lines.get(idx);
            System.err.println(d);
        });
    }

    private void writeFile() {
        File f = new File("test.hex");
        try {
            if (f.createNewFile()) {
                String s = "[FISCAS] machine code saved to "+f.getName();
                System.out.println(s);
            }
            FileWriter w = new FileWriter("test.hex");
            w.write("v2.0 raw\n");
            for (int x : instructionSet) {
                long totalMem = Runtime.getRuntime().totalMemory();
                if (Files.size(Path.of("test.hex")) > totalMem) {
                    String s = "[ERROR] Outfile is larger than sys. memory";
                    throw new Exception(s);
                }
                w.write("" + Integer.toHexString(x) + "\n");
            }
            w.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        String filename = "";
        boolean printTables = false;
        for (String s : args) {
            if (s.contains(".s")) {
                filename = s;
            }
            if (s.contains("-l")) {
                printTables = true;
            }
        }
        System.out.println("[FISCAS] Loading source file "+filename);
        new Fiscas(filename, printTables);
    }
}