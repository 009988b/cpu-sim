start:  not r0 r1       ; let r3 = sum let r1 = counter let r2 = b/comparator let r0 = a
        and r0 r0 r1    ; value and its complement are zero
        not r1 r0       ; r1 contains 255
        add r1 r1 r1    ; r1 contains 254
        not r1 r1       ; not 254 = 1
        and r3 r0 r0    ; let r3 be sum has 0 r0 = F(0) = 0
        add r2 r1 r0
loop:   and r2 r2 r2    ; r2 = b starts at 1
        add r3 r0 r2    ; sum = a+b
        and r0 r2 r2    ; a=b
        not r2 r3       ; before we assign b - increment r1
        and r2 r2 r3    ; value and its complement are zero
        not r2 r2       ; r2 contains 255
        add r2 r2 r2    ; r2 contains 254
        not r2 r2       ; r2 contains 1
        add r1 r1 r2    ; increment counter
        add r2 r2 r2    ; r2 contains 2
        add r2 r2 r2    ; r2 contains 4
        add r2 r2 r2    ; r2 contains 8
        add r2 r2 r2    ; r2 contains 16
        not r2 r2       ; r2 contains 239
        add r2 r1 r2    ; r2 contains 239+counter
        not r2 r2       ; if 239+counter=255, not 255 = 0
        bnz next
halt:   not r2 r2       ; if we have zero, not it to get FF so CPU halts indefinitely
        bnz halt
next:   and r1 r1 r1
        and r2 r3 r3    ; r2 contains b=sum
        bnz loop
