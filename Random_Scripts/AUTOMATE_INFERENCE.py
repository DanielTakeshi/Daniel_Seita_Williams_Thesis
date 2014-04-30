# This will automate some of my inferences.
# (c) 2014 by Daniel Seita

import subprocess

list_nums = []
for i in range(52, 59+1):
    list_nums.append(i)
for i in list_nums:
    # MODIFY the path directory for default, ivga, etc.
    command = "java -Xmx1024m -cp bin exp.inference.SPNInfTest DATA " + str(i) + " N CUSTOM_DATA_SPNs/ivga." + str(i) + ".spn"
    subprocess.call(command.split())
