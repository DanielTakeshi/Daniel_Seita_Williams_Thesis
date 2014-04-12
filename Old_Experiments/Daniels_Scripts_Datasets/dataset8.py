'''
Dataset idea: Let's just RANDOMIZE EVERYTHING! Take that! Modify the loop inside 'generate_sample' to adjust the quantities.

(c) 2014 by Daniel Seita
'''

import random, sys

# Okay, our sampling method.
def generate_sample():
    return_string = ""
    for i in range(19):
        return_string += str(random.randint(0,1)) + ","
    return_string += str(random.randint(0,1))
    return return_string

# MAIN
if (len(sys.argv) < 5):
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
