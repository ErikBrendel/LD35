/*
 *  Copyright 2016 
 *  Markus Brand and Erik Brendel, Potsdam.
 *  This File is part of a game created
 *  for LudumDare 35.
 */
package util;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Erik
 * @param <T> the Data-Type of the List
 */
public class SortedArrayList<T> extends ArrayList<T> {
	
	/**
	 * insert an element by trying to keep the sorting order.
	 * 
	 * The list is garantueed to be sorted
	 * @param value the new Element
	 */
    public void insertSorted(T value){
        add(value);
        Comparable<T> cmp = (Comparable<T>) value;
        for (int i = size() - 1; i > 0 && cmp.compareTo(get(i - 1)) < 0; i--) {
            Collections.swap(this, i, i - 1);
        }
    }
}
