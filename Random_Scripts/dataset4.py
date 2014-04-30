'''
I'm going to try and come up with a dataset that can enforce dependencies WITHOUT relying on any sort of binarization process.
in other words, the datasets produced from here should directly go into the input to the SPN code.
(c) 2014 by Daniel Seita
'''

import random, sys

# Okay, let's try just using XORs.
def generate_sample():
    var1a = random.randint(0,1)
    var1b = random.randint(0,1)
    var2a = random.randint(0,1)
    var2b = random.randint(0,1)
    var1c = var1a ^ var1b
    var2c = var2a ^ var2b
    return str(var1a) + ',' + str(var1b) + ',' + str(var1c) + ',' + str(var2a) + ',' + str(var2b) + ',' + str(var2c)

# MAIN
if (len(sys.argv) != 5):
    print('Error: Incorrect number of arguments.')
    sys.exit(-1)

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
