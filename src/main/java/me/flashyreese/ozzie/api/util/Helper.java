/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.flashyreese.ozzie.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Helper {

    public static long[] appendArray(long[] array, long x) {
        long[] result = new long[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[result.length - 1] = x;
        return result;
    }

    public static long[] unappendArray(long[] array, long value) {//LOL this is stupid but works xd
        long[] result = new long[array.length - 1];
        int offset = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != value) {
                result[i + offset] = array[i];
            } else {
                offset = -1;
            }
        }
        return result;
    }

    public static List<String> asyncListStringWithMaxSize(List<String> list, int size) {
        List<String> outputList = new ArrayList<>();
        Stack<String> stack = new Stack<String>();
        for (int i = list.size() - 1; i >= 0; i--) {
            stack.push(list.get(i));
        }
        int count = 0;
        StringBuilder output = new StringBuilder();
        while (!stack.isEmpty()) {
            if (stack.peek().length() > size) {
                outputList.add(output.toString());
                output = new StringBuilder();
                count = 0;
                String popped = stack.pop();
                String grabSize = popped.substring(0, size - 1);
                stack.push(popped.substring(size, popped.length() - 1));
                outputList.add(grabSize);
            }
            if (count + stack.peek().length() <= size) {
                output.append(stack.peek()).append("\n");
                count += stack.pop().length();
            } else {
                outputList.add(output.toString());
                output = new StringBuilder();
                count = 0;
            }
        }
        return outputList;
    }

    /*public static String convertYamlToJson(String yaml) throws JsonProcessingException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }*/
}
