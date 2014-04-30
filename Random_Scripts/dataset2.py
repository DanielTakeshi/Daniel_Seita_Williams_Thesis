'''
Dataset for the second scenario as described in my file.
(c) 2014 by Daniel Seita
USAGE: $ dataset2.py [train] [valid] [test] [name] [prob] 

Note: to prevent confusion, it's always train - valid - test, in that order.
Note 2: Last command line argument is the chance that we will MAKE red0 = blue0, etc.
Notice that this is NOT the same as the TOTAL PROBABILITY that those are equal...
Also, I'm going to do separate probabilities for the red0 = blue0 and red1 = blue1 pairings

[prob] should be an integer between 1 and 100.
'''

import random, sys

# Computes desired samples based on our objective of a dataset
def generate_sample(prob):
    random_num_0 = random.randint(1,100)
    random_num_1 = random.randint(1,100)
    red0 = random.randint(1,6)
    red1 = random.randint(1,6)

    # Have separate cases for the {red0, blue0} and {red1, blue1} pairings
    if (random_num_0 <= prob):
        blue0 = red0
    else:
        blue0 = random.randint(1,6)
    if (random_num_1 <= prob):
        blue1 = red1
    else:
        blue1 = random.randint(1,6)
    red2 = red0 + red1
    blue2 = blue0 + blue1
    return str(red0) + "," + str(red1) + "," + str(red2) + "," + str(blue0) + "," + str(blue1) + "," + str(blue2)
 
# Main method:
if (len(sys.argv) != 6):
    print('Error: Incorrect number of arguments.')
    sys.exit(-1)

# Goal is to write to files: data_name.{ts, train, valid}.data
num_train = int(sys.argv[1])
num_valid = int(sys.argv[2])
num_test = int(sys.argv[3])
data_name = sys.argv[4]
prob_force_equal = int(sys.argv[5])
file1 = data_name + '.ts.data'
file2 = data_name + '.valid.data'
file3 = data_name + '.test.data'

# Now we generate the data for each of the three files
with open(file1, 'w') as f1:
    for i in range(num_train):
       f1.write(generate_sample(prob_force_equal) + '\n')
with open(file2, 'w') as f2:
    for i in range(num_valid):
        f2.write(generate_sample(prob_force_equal) + '\n') 
with open(file3, 'w') as f3: 
    for i in range(num_test):
        f3.write(generate_sample(prob_force_equal) + '\n') 
