/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nijiko.data.harc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.nijiko.data.harc.util.Common;

/**
    Copyright (c) 2011, Nijiko Yonskai (Nijikokun) <nijikokun@gmail.com>
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        1. Redistributions of source code must retain the above copyright
            notice, this list of conditions and the following disclaimer.

        2. Redistributions in binary form must reproduce the above copyright
            notice, this list of conditions and the following disclaimer in the
            documentation and/or other materials provided with the distribution.

        3. Neither the name of Nijiko Yonskai nor the
            names of its contributors may be used to endorse or promote products
            derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL Nijiko Yonskai BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Main {
    private Common common = new Common();
    LinkedList<String> Groups = new LinkedList<String>();
    LinkedHashMap<Integer, LinkedHashMap<Integer, String>> Weights = new LinkedHashMap<Integer, LinkedHashMap<Integer, String>>();
    LinkedHashMap<String, LinkedList<String>> Inheritance = new LinkedHashMap<String, LinkedList<String>>(), Nodes = new LinkedHashMap<String, LinkedList<String>>();
    LinkedHashMap<String, LinkedHashMap<String, String>> Data = new LinkedHashMap<String, LinkedHashMap<String, String>>();

    public void parse(String file) {
        List<String> lines = common.read(file); // for external files: common.readFile(file);
        LinkedList<String> nodes = new LinkedList<String>();
        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
        String[] split = null, gsplit = null;
        boolean inBlock = false, inCommentBlock = false, inWeighting = false;
        String currentGroup = "";
        int passes = 0, loops = 0, id = 0;


        for(String line: lines) {
            String trimmed = line.trim();

            // Remove empty lines, Comments, Etc
            if(trimmed.startsWith("#") || trimmed.startsWith("--") || trimmed.startsWith("//")) continue;
            if(trimmed.startsWith("/*")) { inCommentBlock = true; continue; }
            if(trimmed.contains("*/") && inCommentBlock) { inCommentBlock = false; continue; }
            if(inCommentBlock) continue;
            if(trimmed.isEmpty()) continue;

            // Comments. Oh Jesus.
            trimmed = common.trimAfter(trimmed, "#");
            trimmed = common.trimAfter(trimmed, "--");
            trimmed = common.trimAfter(trimmed, "//");

            // Clean up nodes
            if(!nodes.isEmpty()) nodes = new LinkedList<String>();

            // Check for endings
            if(trimmed.equals("];") || trimmed.equals("]") || (inBlock && trimmed.endsWith("["))) {
                if(!data.isEmpty() && !currentGroup.isEmpty()) Data.put(currentGroup, data);
                if(inBlock) inBlock = false;
                if(inWeighting) inWeighting = false;
                if(!currentGroup.isEmpty()) currentGroup = "";
                if(!data.isEmpty()) data = new LinkedHashMap<String, String>();

                // Clear the passes for the next block
                passes = 0;
                
                // Continue only if we are sure someone didn't forget to end a block
                // If not we continue to start the new block.
                if(!trimmed.endsWith("[")) continue;
            }

            // We are in a block, the only time we aren't is never.
            if(!inBlock) inBlock = true;

            // Special Block?
            if(trimmed.toLowerCase().startsWith("heirarchy")) {
                inWeighting = true;
                continue;
            }

            if(inWeighting) {
                LinkedHashMap<Integer, String> Weight = new LinkedHashMap<Integer, String>();

                if(!trimmed.contains(">")) {
                    Weight.put(passes, trimmed);
                    Weights.put(passes, Weight);
                    passes++; continue;
                }

                if(trimmed.contains(">")) {
                    split = trimmed.split(">");
                    split = common.trim(split);
                    id = 0;
                    loops = 0;

                    for(String group: split) {
                        Weight = (passes == 0) ? new LinkedHashMap<Integer, String>() : Weights.get(loops);
                        id = (passes == 0) ? 0 : Weights.get(loops).size();

                        if(group.contains("|")) {
                            gsplit = group.split("\\|");
                            gsplit = common.trim(gsplit);
                        } else {
                            gsplit = new String[] { group };
                        }

                        for(String item: gsplit) {
                            if(Weight.containsValue(item)) continue;
                            
                            Weight.put(id, item.replace(";", ""));
                            id++;
                        }

                        Weights.put(loops, Weight);
                        loops++;
                    }
                }

                // System.out.println(Weights.get(0));
                passes++; continue;
            }

            if(passes == 0 && trimmed.endsWith("[")) {
                trimmed = trimmed.substring(0, trimmed.length()-1);
                
                if(!trimmed.contains(">")) {
                    String group = trimmed.split(" ", 2)[0];

                    if(group.contains(".main")) {
                        if(!Weights.isEmpty())
                            Weights.get(0).put(0, group.replace(".main", "").trim());
                        else {
                            LinkedHashMap<Integer, String> Weight = new LinkedHashMap<Integer, String>();
                            Weight.put(0, group.replace(".main", "").trim());
                            Weights.put(0, Weight);
                        }
                    }

                    Groups.add(group.trim());
                    currentGroup = group.trim();
                    passes++;
                }

                if(trimmed.contains(">")) {
                    LinkedList<String> groups = new LinkedList<String>();
                    split = trimmed.split(">");
                    split = common.trim(split);
                    loops = 0;

                    for(String part: split) {
                        if(part == null || part.isEmpty()) continue;

                        if(part.contains("&")) {
                            gsplit = part.split("&");
                            gsplit = common.trim(gsplit);
                        } else {
                            gsplit = new String[] { part.trim() };
                        }

                        for(String group: gsplit) {
                            if(group == null || group.isEmpty()) continue;
                            if(loops == 0) {
                                Groups.add(group);
                                currentGroup = group.trim();
                            } else {
                                groups.add(group);
                            }

                            loops++;
                        }
                    }

                    if(!groups.isEmpty()) Inheritance.put(currentGroup, groups);
                }

                // System.out.println(currentGroup);
                continue;
            }

            // Anything beyond this point needs the current group.
            if(currentGroup.isEmpty()) continue;

            // Remove the nodes: prefix, not needed, just there for letting me sleep at night I guess.
            if(trimmed.startsWith("nodes:")) trimmed = trimmed.replace("nodes:", "").trim();

            if(trimmed.contains(":")) {
                split = trimmed.split(":");
                split = common.trim(split);

                if(split.length < 1) continue;
                if(split[1].endsWith(";")) split[1] = split[1].substring(0, split[1].length()-1);

                String key = split[0];
                String value = split[1];
                
                // Remove the semi-colon ending
                if(value.endsWith(";")) value = value.substring(0, value.length()-1);

                // Remove quotes
                key = common.unQuote(key);
                value = common.unQuote(value);

                // Check the key validity
                if(key.isEmpty()) continue;

                data.put(key, value);
                continue;
            }

            if(trimmed.contains(" ")) {
                split = trimmed.split(",");
                split = common.trim(split);

                for(String node: split) {
                    if(node.endsWith(";")) node = node.substring(0, node.length()-1);

                    nodes.add(node);
                }
            } else {
                if(trimmed.isEmpty()) continue;

                nodes.add(trimmed);
            }

            if(!nodes.isEmpty() && Nodes.containsKey(currentGroup)) {
                nodes.addAll(Nodes.get(currentGroup));
                Nodes.put(currentGroup, nodes);
            } else if(!nodes.isEmpty()) {
                Nodes.put(currentGroup, nodes);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.parse("resources/group.harc");
        System.out.println(main.Inheritance);
        System.out.println(main.Data);
        System.out.println(main.Nodes);
    }

}
