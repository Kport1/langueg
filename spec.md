##Spec for langueg

---
##Syntax

###Code Blocks
Code blocks are written using curly braces (`{ }`). This also applies to code blocks in loops and functions.

###Primitive types
- unsigned byte
- signed short
- signed int
- signed long
- float
- double

###Function types
Function types must specify types of the arguments passed into the function and its return type.\
For one argument of type `x` and a return type of `y`:\
`Fn[x -> y]`

For two arguments of type `x0` and `x1` and a return type of `y`:\
`Fn[(x0, x1) -> y]`

###Functions
A function with return type `y`, argument `x` of type `t` and name `name` can eiter:
1. Use a block as its body and have a return statement:
    ````
    fn y name(t x) {
      return ...;
    }
    ````
2. Use an expression as its body and have a return statement:
    ````
    fn y name(t x)
        return ...;
    ````
   
3. Use an expression as its body and not have a return statement:

    ````
    fn y name(t x) ...;
    ````
   
###Variables
To declare a variable of type `t` and name `name` write:\
`t name;`

To declare a variable and initialize it with value `v` (witch may be another expression) write:\
`t name = v;`

###If statement
1. If statement of condition `c` without `else` block:
   ````
   if(c){
      ...
   }
   ````

2. If statement of condition `c` with `else` block:
   ````
   if(c){
      ...
   } else {
      ...
   }
   ````
   
###While loop
A while loop may be head or foot controlled.
1. Head controlled loop with condition `c`:\
`while(c) { ... }`


2. Foot controlled loop with condition `c`:\
   `do { ... } while(c)`