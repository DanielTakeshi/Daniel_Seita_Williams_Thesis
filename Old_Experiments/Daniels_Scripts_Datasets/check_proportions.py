# This code will check proportions for Python code to make sure that there are fewer of those 000000s and 111111s.
# (c) February 2014, by Daniel Seita

import sys

if (len(sys.argv) != 2):
    print('USAGE: $ python check_proportions.py [file_name]')
    sys.exit()

with open(sys.argv[1], 'r') as file_to_check:
    total_lines = 0
    total_zeroes = 0
    total_ones = 0
    for current_line in file_to_check:
        total_lines += 1
        line = current_line.strip()
        if (line == '0,0,0,0,0,0'):
            total_zeroes += 1
        elif (line == '1,1,1,1,1,1'):
            total_ones += 1
print('Total lines: {}, all zeroes: {} ({}%), all ones: {} ({}%)'.format(total_lines, total_zeroes, 100 * float(total_zeroes)/total_lines, total_ones,
                                                                                                    100 * float(total_ones)/total_lines))
