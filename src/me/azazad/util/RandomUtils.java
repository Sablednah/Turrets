package me.azazad.util;

import java.util.Collection;
import java.util.Random;

public final class RandomUtils{
    private RandomUtils(){}
    
    /**
     * Lifted from http://stackoverflow.com/questions/124671/picking-a-random-element-from-a-set, generics added
     */
    public static <T> T randomElement(Collection<T> collection,Random random){
        int index = random.nextInt(collection.size());
        int i = 0;
        for(T element : collection){
            if(i == index){
                return element;
            }
            i++;
        }
        
        return null;
    }
}