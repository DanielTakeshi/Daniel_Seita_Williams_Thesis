'''
I'm going to try and come up with a dataset that can enforce dependencies WITHOUT relying on any sort of binarization process.
in other words, the datasets produced from here should directly go into the input to the SPN code.
(c) 2014 by Daniel Seita

How about just forcing things to be equal, then randomize
'''

import random, sys

# Okay, let's try just using XORs.
def generate_sample(prob):
    randgroup1 = random.randint(1,100)   
    randgroup2 = random.randint(1,100)   
    randgroup3 = random.randint(1,100)   
    randgroup4 = random.randint(1,100)   
    var1a = random.randint(0,1)
    var2a = random.randint(0,1)
    var3a = random.randint(0,1)
    var4a = random.randint(0,1)
    if (randgroup1 <= 75):   
        var1b = var1a
        var1c = var1a
    else:
        var1b = random.randint(0,1)
        var1c = random.randint(0,1)
    if (randgroup2 <= 75):   
        var2b = var1a
        var2c = var1a
    else:
        var2b = random.randint(0,1)
        var2c = random.randint(0,1)
    if (randgroup3 <= 75):   
        var3b = var1a
        var3c = var1a
    else:
        var3b = random.randint(0,1)
        var3c = random.randint(0,1)
    if (randgroup4 <= 75):   
        var4b = var1a
        var4c = var1a
    else:
        var4b = random.randint(0,1)
        var4c = random.randint(0,1)

    
    first_part = str(var1a) + ',' + str(var1b) + ',' + str(var1c) + ',' + str(var2a) + ',' + str(var2b) + ',' + str(var2c)
    second_part = str(var3a) + ',' + str(var3b) + ',' + str(var3c) + ',' + str(var4a) + ',' + str(var4b) + ',' + str(var4c)
    return first_part + ',' + second_part

# MAIN
probability_force_same = 0.75

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
        f1.write(generate_sample(probability_force_same) + '\n')
with open(file2, 'w') as f2:
    for i in range(num_valid):
        f2.write(generate_sample(probability_force_same) + '\n')
with open(file3, 'w') as f3:
    for i in range(num_test):
        f3.write(generate_sample(probability_force_same) + '\n')
