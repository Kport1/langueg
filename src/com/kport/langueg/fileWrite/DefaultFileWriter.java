package com.kport.langueg.fileWrite;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.FnData;
import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.util.FnIdentifier;
import com.sun.jdi.InvalidTypeException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultFileWriter implements FileWriter{
    private static final byte[] MAGIC = {'l', 'a', 'l', 'a'};
    private final String path;

    public DefaultFileWriter(String path_){
        path = path_;
    }


    @Override
    @SuppressWarnings("unchecked")
    public Void process(Object input, LanguegPipeline<?, ?> pipeline) {

        CodeGenState state;
        try {
            state = pipeline.getAdditionalData("State", CodeGenState.class);
        }
        catch (InvalidTypeException e){
            e.printStackTrace();
            throw new Error();
        }



        return null;
    }

    @Override
    public String getFilePath(){
        return path;
    }
}
