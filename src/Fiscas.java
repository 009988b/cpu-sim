import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Fiscas {
    private final ArrayList<Integer> instructionSet;
    private final ArrayList<String> lines;
    private final HashMap<String, Integer> labels;

    private int cursorOffset = 0;           // Must count empty lines
                                            // and comment-only lines

    public Fiscas(String sourceFileName, String outFileName, boolean printTables) {
        instructionSet = new ArrayList<>();
        lines = new ArrayList<>();
        labels = new HashMap<>();
        if (sourceFileName == "") {
            System.err.println("[ERROR] No source .s file provided");
            System.exit(-1);
        }
        readFile(sourceFileName);
        if (outFileName != "") writeFile(outFileName);
        else writeFile("a.hex");
        if (printTables) {
            printTables();
        }
    }

    private String encodeRegister(String r) throws Exception {
        String x;
        switch (r) {
            case "r0":
                x = "00";
                break;
            case "r1":
                x = "01";
                break;
            case "r2":
                x = "10";
                break;
            case "r3":
                x = "11";
                break;
            default:
                    throw new Exception("[ERROR] Invalid register: " + r);
        }
        return x;
    }

    private int parseCommand(String cmd) {
        String result;
        try {
            cmd = cmd.trim();
            if (cmd.contains(",")) {
                String msg = "[ERROR] Invalid assembler line: "+cmd;
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
                if (c.length < 2) return -1;
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
                case "add":
                    result = "00";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += encodeRegister(rm);
                    break;
                case "and":
                    result = "01";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += encodeRegister(rm);
                    break;
                //return Integer.parseInt(result,2);

                case "not":
                    result = "10";
                    result += encodeRegister(rd);
                    result += encodeRegister(rn);
                    result += "00";
                    break;
                case "bnz":
                    result = "11";
                    if (labels.get(c[1]) == null) {
                        String msg = "[ERROR] Undefined label: " + c[1];
                        throw new Exception(msg);
                    }
                    String target = Integer.toBinaryString(
                            labels.get(c[1]));
                    int l = 6 - target.length();
                    for (int x = 0; x<l; x++) {
                        result += "0";
                    }
                    int s = lines.size();
                    if (Integer.parseInt(target, 2) > s) {
                        String msg = "[ERROR] Target address out";
                        msg += "of bounds: 0b" + target + " max: " + s;
                        throw new Exception(msg);
                    }
                    result += target;
                    break;
                default:
                    String msg = "[ERROR] Instruction {" + i + "} not";
                    msg += " recognized in set {add, and, not, bnz}";
                    throw new Exception(msg);
            }
            return Integer.parseInt(result, 2);
        } catch (Exception e) {
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
                if (!line.isEmpty() && !line.trim().startsWith(";"))
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
                if (line.trim().startsWith(";") || line.isEmpty()) {
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

    private void writeFile(String o) {
        File f = new File(o);
        try {
            if (f.createNewFile()) {
                String s = "[FISCAS] Created "+f.getName();
                System.out.println(s);
            } else {
                String err = "[ERROR] File "+f.getName()+" already exists!";
                throw new Exception(err);
            }
            FileWriter w = new FileWriter(o);
            w.write("v2.0 raw\n");
            for (int x : instructionSet) {
                if (Files.lines(Paths.get(o)).count() > 64) {
                    String s = "[ERROR] Outfile is larger than 64x8";
                    throw new Exception(s);
                }
                w.write("" + Integer.toHexString(x) + "\n");
            }
            w.close();
            String s = "[SUCCESS] Saved to "+f.getName();
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        String filename = "";
        String outputName = "a.hex"; // Default if no outfile provided
        boolean printTables = false;
        for (String s : args) {
            if (s.contains(".s")) {
                filename = s;
            }
            if (s.contains("-l")) {
                printTables = true;
            }
            if (s.contains(".hex")) {
                outputName = s;
            }
        }
        System.out.println("[FISCAS] Loading source file "+filename);
        String msg = "[FISCAS] No output file name provided. ";
        msg += "Defaulting to a.hex";
        if (outputName == "a.hex") System.out.println(msg);
        new Fiscas(filename, outputName, printTables);
    }
}