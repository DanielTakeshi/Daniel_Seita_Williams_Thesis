'''
Dataset for the first scenario as described in my file.
(c) 2014 by Daniel Seita
USAGE: $ dataset1.py [train] [valid] [test] [name] 

Note: to prevent confusion, it's always train - valid - test, in that order.
'''

import random, sys

# Computes desired samples based on our objective of a dataset
def generate_sample():
    red0 = random.randint(1,6)
    red1 = random.randint(1,6)
    blue0 = random.randint(1,6)
    blue1 = random.randint(1,6)
    red2 = red0 + red1
    blue2 = blue0 + blue1
    return str(red0) + "," + str(red1) + "," + str(red2) + "," + str(blue0) + "," + str(blue1) + "," + str(blue2)
 
# Main method:
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
