package me.wilsonhu.ozzie.utilities;

public class Helper {

    public static long[] appendArray(long[] array, long x){
        long[] result = new long[array.length + 1];
        for(int i = 0; i < array.length; i++){
            result[i] = array[i];
        }
        result[result.length - 1] = x;
        return result;
    }

    public static long[] unappendArray(long[] array, long value){//LOL this is stupid but works xd
        long[] result = new long[array.length-1];
        int offset = 0;
        for(int i = 0; i < array.length; i++){
            if(array[i] != value){
                result[i+offset] = array[i];
            }else{
                offset = -1;
            }
        }
        return result;
    }
}
