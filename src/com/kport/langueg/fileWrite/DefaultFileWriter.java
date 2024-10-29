package com.kport.langueg.fileWrite;

import com.kport.langueg.codeGen.languegVmCodeGen.CodeGenState;
import com.kport.langueg.codeGen.languegVmCodeGen.FnData;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.util.CodeOutputStream;
import com.sun.jdi.InvalidTypeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DefaultFileWriter implements FileWriter{
    private static final byte[] MAGIC = {'g', 'u', 'e', 'g'};
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

        outputStream.writeShort((short) state.constIndices.size());
        for (byte[] val : state.constIndices.sequencedKeySet()) {
            outputStream.writeShort((short)val.length);
            outputStream.writeBytes(val);
        }

        outputStream.writeShort((short) state.generatedFns.size());
        for (FnData fn : state.generatedFns) {
            outputStream.writeShort(fn.paramLocalsSize);
            outputStream.writeShort(fn.retLocalsSize);

            outputStream.writeShort((short)fn.localsSize);

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
