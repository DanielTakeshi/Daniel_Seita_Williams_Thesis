'''
I need to binarize the data, then.
(c) 2014 by Daniel Seita
This code is meant to only work on the simple dice examples I gave. It takes in the file name and will output a new file that creates six binary
values for each of the six values of the dice, creating 24 variables from the single dice rolls. Then for the sums, it splits them into four bins
based on their frequency.
'''

import sys

# Given n \in {1,2,3,4,5,6}, returns a length-6, binarized string.
def binarize_six(n):
    return_list = [0,0,0,0,0,0]
    return_list[int(n)-1] = 1
    return ','.join(map(str, return_list)) # Gets it in the right form I want

# Keep it simple for now. Split up the sums into {2,3,4,5}, {6,7,8}, and {9,10,11,12}.
def binarize_sum(n):
    return_list = [0,0,0]
    if (int(n) <= 5):
        return_list[0] = 1
    elif (int(n) <= 8):
        return_list[1] = 1
    else:
        return_list[2] = 1
    return ','.join(map(str, return_list))

# MAIN
if (len(sys.argv) != 2):
    print('Incorrect number of arguments')
    sys.exit(-1)

old_name_list = sys.argv[1].split('.')
new_string = old_name_list[0] + '.binarized.' + old_name_list[1] + '.data'
with open(sys.argv[1], 'r') as old_file:
    with open(new_string, 'w') as new_file:
        # For each line in the old file, must create new line and insert into new file
        for line in old_file:
            new_line = ''
            old_line_list = line.split(',')
            # Red dice
            new_line += binarize_six(old_line_list[0]) + ',' + binarize_six(old_line_list[1]) + ','
            new_line += binarize_sum(old_line_list[2]) + ','
            # Blue dice
            new_line += binarize_six(old_line_list[3]) + ',' + binarize_six(old_line_list[4]) + ','
            new_line += binarize_sum(old_line_list[5])
            new_file.write(new_line + '\n')
