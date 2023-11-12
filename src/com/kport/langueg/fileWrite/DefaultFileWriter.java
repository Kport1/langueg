package com.kport.langueg.fileWrite;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.FnData;
import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.Type;
import com.sun.jdi.InvalidTypeException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class DefaultFileWriter implements FileWriter{
    private static final byte[] MAGIC = {'l', 'a', 'l', 'a'};
    private final Path path;

    public DefaultFileWriter(Path path_){
        path = path_;
    }


    @Override
    public Void process(Object input, LanguegPipeline<?, ?> pipeline) {

        CodeGenState state;
        try {
            state = pipeline.getAdditionalData("State", CodeGenState.class);
        }
        catch (InvalidTypeException e){
            e.printStackTrace();
            throw new Error();
        }

        try(FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            outputStream.write(MAGIC);

            {
                byte[] modNameBytes = state.modInterface.name().getBytes(StandardCharsets.US_ASCII);
                if (modNameBytes.length > 255) throw new Error();

                outputStream.write(modNameBytes.length);
                outputStream.write(modNameBytes);
            }

            outputStream.write(state.modInterface.returnType().serialize());

            {
                Type[] modParamTypes = state.modInterface.getParamTypes();
                if (modParamTypes.length > 255) throw new Error();

                outputStream.write(modParamTypes.length);
                for (Type modParamType : modParamTypes) {
                    outputStream.write(modParamType.serialize());
                }
            }

            outputStream.write(state.const32List.size());
            outputStream.write(state.const32List.size() >>> 8);
            for (Integer val : state.const32List) {
                outputStream.write(val);
                outputStream.write(val >>> 8);
                outputStream.write(val >>> 16);
                outputStream.write(val >>> 24);
            }

            outputStream.write(state.const64List.size());
            outputStream.write(state.const64List.size() >>> 8);
            for (Long l : state.const64List) {
                long val = l;
                outputStream.write((int)val);
                outputStream.write((int)(val >>> 8));
                outputStream.write((int)(val >>> 16));
                outputStream.write((int)(val >>> 24));
                outputStream.write((int)(val >>> 32));
                outputStream.write((int)(val >>> 40));
                outputStream.write((int)(val >>> 48));
                outputStream.write((int)(val >>> 56));
            }

            outputStream.write(state.generatedFns.size());
            outputStream.write(state.generatedFns.size() >>> 8);
            for (FnData fn : state.generatedFns) {
                outputStream.write(LanguegVmValSize.codeOf(fn.returnValSize));

                outputStream.write(fn.paramValSizes.length);
                for (LanguegVmValSize param : fn.paramValSizes) {
                    outputStream.write(LanguegVmValSize.codeOf(param));
                }

                for (LanguegVmValSize size : LanguegVmValSize.values()) {
                    outputStream.write(fn.amntLocals.get(size));
                    outputStream.write(fn.amntLocals.get(size) >>> 8);
                }

                for (LanguegVmValSize size : LanguegVmValSize.values()) {
                    outputStream.write(fn.maxStackDepth.get(size));
                    outputStream.write(fn.maxStackDepth.get(size) >>> 8);
                }

                outputStream.write(fn.code.size());
                outputStream.write(fn.code.size() >>> 8);
                outputStream.write(fn.code.size() >>> 16);
                outputStream.write(fn.code.size() >>> 24);
                outputStream.write(fn.code.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    @Override
    public Path getFilePath(){
        return path;
    }
}
