package com.kport.langueg.fileWrite;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.FnData;
import com.kport.langueg.codeGen.languegVmCodeGen.LanguegVmValSize;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.Type;
import com.kport.langueg.util.CodeOutputStream;
import com.sun.jdi.InvalidTypeException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

        CodeOutputStream outputStream = new CodeOutputStream();
        outputStream.writeBytes(MAGIC);

        outputStream.writeShort((short) state.const32List.size());
        for (Integer val : state.const32List) {
            outputStream.writeInt(val);
        }

        outputStream.writeShort((short) state.const64List.size());
        for (Long l : state.const64List) {
            outputStream.writeLong(l);
        }

        outputStream.writeShort((short) state.generatedFns.size());
        for (FnData fn : state.generatedFns) {
            outputStream.write(LanguegVmValSize.codeOf(fn.returnValSize));

            outputStream.write(fn.paramValSizes.length);
            for (LanguegVmValSize param : fn.paramValSizes) {
                outputStream.write(LanguegVmValSize.codeOf(param));
            }

            for (LanguegVmValSize size : LanguegVmValSize.values()) {
                outputStream.writeShort(fn.amntLocals.get(size).shortValue());
            }

            for (LanguegVmValSize size : LanguegVmValSize.values()) {
                outputStream.writeShort(fn.maxStackDepth.get(size).shortValue());
            }

            outputStream.writeInt(fn.code.size());
            outputStream.writeBytes(fn.code.toByteArray());
        }

        try(FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            channel.write(ByteBuffer.wrap(outputStream.toByteArray()));
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
