package com.kport.langueg.fileWrite;

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

        byte[] constIndices;
        Map<FnIdentifier, FnData> fnData;
        try {
            constIndices = pipeline.getAdditionalData("ConstIndices", byte[].class);
            fnData = pipeline.getAdditionalData("FnData", Map.class);
        }
        catch (InvalidTypeException e){
            e.printStackTrace();
            throw new Error();
        }

        try {
            FileOutputStream output = new FileOutputStream(path);
            output.write(MAGIC);
            output.write(constIndices);

            fnData.forEach((id, data) -> {

                System.out.println(id + "  " + data);

            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getFilePath(){
        return path;
    }
}
