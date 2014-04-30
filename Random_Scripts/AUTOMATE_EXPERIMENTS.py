# This will automate some of my experiments. This is for BUILDING SPNs.
# (c) 2014 by Daniel Seita

import subprocess

# 29-43 random, 44-51 andor, 52-59 dice
list_nums = []
for i in range(29, 43+1):
    list_nums.append(i)
for i in list_nums:
    file_name = "d" + str(i) + "_dbscan_random.spn"
    command = "java -Xmx1024m -cp bin exp.RunSLSPN DATA " + str(i) + " GF 10 CP 0.6 INDEPINST 4 N " + file_name
    subprocess.call(command.split())
