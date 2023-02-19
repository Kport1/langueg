package com.kport.langueg.codeGen.languegVmCodeGen;

import com.kport.langueg.util.FnIdentifier;

import java.util.EnumMap;

public record FnData(LanguegVmValSize returnValSize, EnumMap<LanguegVmValSize, Integer> amntLocals, EnumMap<LanguegVmValSize, Integer> maxStackDepth, byte[] lineInfo, byte[] code) {
}
