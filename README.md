# Compiler for RecSPL
## Created by Aren Repko (u04515791) and Daniel Geerdink (u22556860)

### Assumptions
- Each token is seperated by a space (parentheses, commas, and semicolons cannot follow directly after another token), a space needs to be between them.
- You may have endlines and tabs
- Text HAS to be surrounded by inverted commas: ```"Example"```

### Output
- At each stage a file is generated for each output:
- ```Tokens.xml``` for the Token Stream or result of the lexer (Part 1)
- ```SyntaxTree.xml``` for the Syntax tree or the result of the parser (Part 2)
- ```SymbolTable.txt``` for the VTable or Symbol table (Part 3)
- Result of the type checking is printed in the console (Part 4)
- ```intermediateCode.txt``` has the intermediate code (Part 5a)

### How to run
- A single .jar file is given (Compiler.jar)
- Place the input file in the same directory as the .jar file
- Use this command in the terminal to run the executable: ```java -jar Compiler.jar```
- The output files will be added to the directory of the executable

### Completed Parts
- Part 1 to Part 5a was completed (Up to intermediate code generation)
