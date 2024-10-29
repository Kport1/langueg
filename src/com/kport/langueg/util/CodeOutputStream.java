package com.kport.langueg.util;

import java.io.ByteArrayOutputStream;

public class CodeOutputStream extends ByteArrayOutputStream {

    public CodeOutputStream() {
        super();
    }

    public CodeOutputStream(int size) {
        super(size);
    }

    public synchronized void writeShort(short s){
        write(s);
        write(s >>> 8);
    }

    public synchronized void writeShort(short s, int index){
        if(index + 1 >= count) throw new IndexOutOfBoundsException();
        buf[index] = (byte) s;
        buf[index + 1] = (byte) (s >>> 8);
    }

    public synchronized void writeInt(int i){
        write(i);
        write(i >>> 8);
        write(i >>> 16);
        write(i >>> 24);
    }

    public synchronized void writeInt(int i, int index){
        if(index + 3 >= count) throw new IndexOutOfBoundsException();
        buf[index] = (byte) i;
        buf[index + 1] = (byte) (i >>> 8);
        buf[index + 2] = (byte) (i >>> 16);
        buf[index + 3] = (byte) (i >>> 24);
    }

    public synchronized void writeLong(long l){
        write((int)l);
        write((int)(l >>> 8));
        write((int)(l >>> 16));
        write((int)(l >>> 24));
        write((int)(l >>> 32));
        write((int)(l >>> 40));
        write((int)(l >>> 48));
        write((int)(l >>> 56));
    }

    public synchronized void writeLong(long l, int index) {
        if (index + 7 >= count) throw new IndexOutOfBoundsException();
        buf[index] = (byte) l;
        buf[index + 1] = (byte) (l >>> 8);
        buf[index + 2] = (byte) (l >>> 16);
        buf[index + 3] = (byte) (l >>> 24);
        buf[index + 4] = (byte) (l >>> 32);
        buf[index + 5] = (byte) (l >>> 40);
        buf[index + 6] = (byte) (l >>> 48);
        buf[index + 7] = (byte) (l >>> 56);
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < count; i++){
            s.append(Integer.toHexString(Byte.toUnsignedInt(buf[i]))).append(", ");
        }
        if(s.length() > 2) s.delete(s.length() - 2, s.length());
        return s.toString();
    }
}
