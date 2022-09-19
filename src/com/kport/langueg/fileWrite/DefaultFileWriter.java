package com.kport.langueg.fileWrite;

import com.kport.langueg.pipeline.LanguegPipeline;
import com.sun.jdi.InvalidTypeException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DefaultFileWriter implements FileWriter{
    private static final byte[] MAGIC = {'l', 'a', 'l', 'a'};
    private final String path;

    public DefaultFileWriter(String path_){
        path = path_;
    }


    @Override
    public Void process(Object input, LanguegPipeline<?, ?> pipeline) {
        byte[] prog = (byte[]) input;

        byte[] lineInfo;
        byte[] constIndices;
        try {
            lineInfo = pipeline.getAdditionalData("LineInfo", byte[].class);
            constIndices = pipeline.getAdditionalData("ConstIndices", byte[].class);
        }
        catch (InvalidTypeException e){
            throw new Error(e.getMessage());
        }

        try {
            FileOutputStream output = new FileOutputStream(getFilePath());
            output.write(MAGIC);
            output.write(constIndices);
            output.write(lineInfo);
            output.write(prog);
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
