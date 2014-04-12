'''
Dataset idea: let's use ANDs and ORs. Data will be of the following form, with six colums of variables.

{0,1} {0,1} {OR} {0,1} {0,1} {AND}

Here, there is an optional parameter to 'smooth' the data, i.e., place less emphasis on having all zeroes, and all ones.

(c) 2014 by Daniel Seita
'''

import random, sys

# Okay, our sampling method.
def generate_sample():
    var1 = random.randint(0,1)
    var2 = random.randint(0,1)
    var3 = var1 or var2
    var4 = random.randint(0,1)
    var5 = random.randint(0,1)
    var6 = var4 and var5
    if (var1 + var2 + var4 + var5 == 0 or var1 + var2 + var4 + var5 == 4):
        # Some percent chance of NOT using all 0s or all 1s
        repeat = random.randint(1,100)
        if (repeat <= 0):
            return generate_sample()
    return str(var1) + ',' + str(var2) + ',' + str(var3) + ',' + str(var4) + ',' + str(var5) + ',' + str(var6) 

# MAIN
smooth_factor = 0
if (len(sys.argv) < 5):
    print('Error: Incorrect number of arguments.')
    sys.exit(-1)
if (len(sys.argv) == 6):
    smooth_factor = int(sys.argv[5])   

# Goal is to write to files: data_name.{ts, train, valid}.data
num_train = int(sys.argv[1])
num_valid = int(sys.argv[2])
num_test = int(sys.argv[3])
data_name = sys.argv[4]
file1 = data_name + '.ts.data'
file2 = data_name + '.valid.data'
file3 = data_name + '.test.data'

# Now we generate the data for each of the three files
with open(file1, 'w') as f1:
    for i in range(num_train):
        f1.write(generate_sample() + '\n')
with open(file2, 'w') as f2:
    for i in range(num_valid):
        f2.write(generate_sample() + '\n')
with open(file3, 'w') as f3:
    for i in range(num_test):
        f3.write(generate_sample() + '\n')
