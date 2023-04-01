start:  not r0 r1
        and r0 r0 r1    ; value and its complement are zero
        not r1 r0       ; r1 contains 255
        add r1 r1 r1    ; r1 contains 254
        not r1 r1       ; not 254 = 1
        and r3 r0 r0    ; let r3 be sum has 0 r0 = F(0) = 0
loop:   add r2 r1 r0    ; r2 = b starts at 1
        add r3 r0 r2    ; sum = a+b
        and r0 r2 r2    ; a=b
        and r2 r3 r3    ; b=sum
        and r2 r2 r2    ; unset zero flag
        bnz loop        ; let r3 = sum let r1 = counter let r2 = b let r0 = a