Purpose:
This program is created to convert a Context free grammar(CFG) 
to chomsky normal form(CNF) 
and greibach normal form(GNF)

The main file will accept the input
Main will call simplifier to simplify the grammar by using the 
three rules
1. Remove Lambda (nullable)
2. Remove unit productions (i.e S -> A)
3. Remove useless productions (non temrinating or unreachables)

Included:
Main.java
Simplifier.java
CNFConverter.java
GNFConverter.java
test1.txt
test2.txt
README.md
.gitignore

Requirements:
IDE of choice
Java sdk 19 or above

Run the following command to compile the files:

> javac *.java

Then the following command to run the program:

> java Main

**Note: It is important to compile the files before running Main**

You will be prompted to enter a file that contains the grammar.

> Enter the grammar file name:

Result from test1.txt:

Original:   {S=[A, B], A=[LAMBDA], B=[aBb, b]}
Simplified: {S=[aBb, b], B=[aBb, b]}
CNF:        {S=[AD, b], B=[AE, b], A=[a], C=[b], D=[BC], E=[BC]}
GNF:        {S=[aD, b], B=[aE, b], A=[a], C=[b], D=[aEC, bC], E=[aEC, bC]}

Result from test2.txt

Original:   {S=[a, aA, B, C], A=[aB, LAMBDA], B=[Aa], C=[cCD], D=[ddd, Cd]}
Simplified: {S=[a]}
CNF:        {S=[a]}
GNF:        {S=[a]}