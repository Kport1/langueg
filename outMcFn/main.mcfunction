data modify storage languegmc:stack val prepend value 1.0f
execute store result storage languegmc:var_x_0_0 val float 0.000000010000000 run data get storage languegmc:stack val[0] 1
data remove storage languegmc:stack val[0]
data modify storage languegmc:stack val prepend value 0.5f
data modify storage languegmc:stack val prepend from storage languegmc:var_x_0_0 val
execute store result score #ARITHMETIC_REG_1 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
scoreboard players operation #ARITHMETIC_REG_1 a *= #ARITHMETIC_REG_2 a
execute store result storage languegmc:tmp1 val float 1 run scoreboard players get #ARITHMETIC_REG_1 a
data modify storage languegmc:stack val prepend from storage languegmc:tmp1 val
execute store result storage languegmc:var_x_0_0 val float 0.000000010000000 run data get storage languegmc:stack val[0] 1
data remove storage languegmc:stack val[0]
data modify storage languegmc:stack val prepend value 0.5f
data modify storage languegmc:stack val prepend from storage languegmc:var_x_0_0 val
execute store result score #ARITHMETIC_REG_1 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
scoreboard players operation #ARITHMETIC_REG_1 a *= #ARITHMETIC_REG_2 a
execute store result storage languegmc:tmp1 val float 1 run scoreboard players get #ARITHMETIC_REG_1 a
data modify storage languegmc:stack val prepend from storage languegmc:tmp1 val
execute store result storage languegmc:var_x_0_0 val float 0.000000010000000 run data get storage languegmc:stack val[0] 1
data remove storage languegmc:stack val[0]
data modify storage languegmc:stack val prepend value 0.5f
data modify storage languegmc:stack val prepend from storage languegmc:var_x_0_0 val
execute store result score #ARITHMETIC_REG_1 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:stack val[0] 10000
data remove storage languegmc:stack val[0]
scoreboard players operation #ARITHMETIC_REG_1 a *= #ARITHMETIC_REG_2 a
execute store result storage languegmc:tmp1 val float 1 run scoreboard players get #ARITHMETIC_REG_1 a
data modify storage languegmc:stack val prepend from storage languegmc:tmp1 val
execute store result storage languegmc:var_x_0_0 val float 0.000000010000000 run data get storage languegmc:stack val[0] 1
data remove storage languegmc:stack val[0]
