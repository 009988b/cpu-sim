import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Fiscas {
    public ArrayList<Integer> instructionSet;
    private HashMap<String, Integer> labels;
    /*test program that counts up to 255
start:  not r0 r1
        and r0 r0 r1    ; value and its complement are zero
        not r1 r0       ; r1 contains 255
        add r1 r1 r1    ; r1 contains 254
        not r1 r1       ; not 254 = 1
loop:   and r3 r0 r0    ; r3 now has zero
        add r0 r0 r1    ; increment by 1
        bnz loop        ; repeat until r0=256=0
        and r3 r3 r3    ; and r3 with itself to set flags
stop:   bnz stop        ; r3 is never zero, effectively stop


   ** EXPECTED OUTPUT **

    v2.0 raw
    90 X
    44 X
    81 X
    15 *
    91 X
    43 X
    04 X
    C5 *
    7F *
    C9 *

    * */

    private String registerToBinaryString(String r) {
        switch (r) {
            case "r0":
                return "00";
            case "r1":
                return "01";
            case "r2":
                return "10";
            case "r3":
                return "11";
        }
        return "00";
    }

    private int parseCommand(String cmd) {
        String result = "";
        cmd = cmd.trim();
        String[] c = cmd.split(" ");
        String i = "";
        String rd = "";
        String rn = "";
        String rm = "";
        if (!cmd.contains(":")) {
            i = c[0];
            if (c.length > 0) {
                rd = c[1];
            }
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
                result+=registerToBinaryString(rd);
                result+=registerToBinaryString(rn);
                result+=registerToBinaryString(rm);
                System.out.println(result);
                break;

            case "and":
                result = "01";
                result+=registerToBinaryString(rd);
                result+=registerToBinaryString(rn);
                result+=registerToBinaryString(rm);
                System.out.println(result);
                break;
                //return Integer.parseInt(result,2);

            case "not":
                result = "10";
                result+=registerToBinaryString(rd);
                result+=registerToBinaryString(rn);
                result+=registerToBinaryString(rm);
                System.out.println(result);
                break;

            case "bnz":
                result = "11";
                String target = Integer.toBinaryString(labels.get(c[1]));
                int l = 6-target.length();
                for (int bit = 0; bit < l; bit++) {
                    result += "0";
                }
                result += target;
                System.out.println(result);
                break;
        }

        return Integer.parseInt(result,2);
    }

    private void readFile(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            int idx = 0;
            while (line != null) {
                //first pass, gather labels
                if (line.contains(":")) {
                    String[] s = line.split(":");
                    String label = s[0];
                    //System.out.println(s[1].trim());
                    int addr = idx;
                    labels.put(label,addr);

                }

                line = reader.readLine();
                idx++;
            }

            reader.close();

            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            idx = 0b0;
            while (line != null) {
                //second pass, translate to machine code
                String cmd = "";
                if (line.contains(";")) {
                    String[] c = line.split(";");
                    if (!line.contains(":")) {
                        cmd = c[0];
                    } else {
                        cmd = c[0].split(":")[1];
                    }
                } else {
                    cmd = line;
                }
                //System.out.print((idx+1)+"\t::\t");
                instructionSet.add(parseCommand(cmd));
                //read next line
                line = reader.readLine();
                idx++;
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFile() {
        File f = new File("a.out");
        try {
            if (f.createNewFile()) {
                System.out.println("File created: " + f.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter w = new FileWriter("a.out");
            w.write("v2.0 raw\n");
            for (int x : instructionSet) {
                w.write(""+Integer.toHexString(x)+"\n");
            }
            w.close();
        } catch (Exception e) {

        }
    }
    public Fiscas(String sourceFileName) {
        instructionSet = new ArrayList<>();
        labels = new HashMap<>();
        readFile(sourceFileName);
        writeFile();
    }
}
