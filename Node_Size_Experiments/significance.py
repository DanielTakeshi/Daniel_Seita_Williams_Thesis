# This will test for significance of my programs.
# (c) 2014 by Daniel Seita
# Takes in two files as arguments. The files should be nothing but numbers.

import sys
from itertools import izip

if len(sys.argv) != 3:
    print("Usage: python significance.py [file1] [file2]")
    sys.exit()

# Reads in both files line by line, so num1 and num2 should correspond to the same test case
with open(sys.argv[1]) as textfile1, open(sys.argv[2]) as textfile2: 
    for num1, num2 in izip(textfile1, textfile2):
        num1 = float(num1.strip())
        num2 = float(num2.strip())
