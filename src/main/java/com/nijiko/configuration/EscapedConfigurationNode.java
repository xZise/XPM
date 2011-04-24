package com.nijiko.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.util.config.ConfigurationNode;

public class EscapedConfigurationNode extends ConfigurationNode{

    protected static final Pattern splitter = Pattern.compile("([^\\\\\\.]*(?:\\\\.[^\\\\\\.]*)*)"); //Anything between non-escaped periods
    protected EscapedConfigurationNode(Map<String, Object> root) {
        super(root);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object getProperty(String path) {
        Matcher m = splitter.matcher(path);
        List<String> parts = new ArrayList<String>();
        while(m.find())
        {
            parts.add(m.group());
        }
        Map<String, Object> node = root;
        
        
        for (int i = 0; i < parts.size(); i++) {
            Object o = node.get(parts.get(i));
            
            if (o == null) {
                return null;
            }
            
            if (i == parts.size() - 1) {
                return o;
            }
            
            try {
                node = (Map<String, Object>)o;
            } catch (ClassCastException e) {
                return null;
            }
        }
        
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setProperty(String path, Object value) {
        Matcher m = splitter.matcher(path);
        List<String> parts = new ArrayList<String>();
        while(m.find())
        {
            parts.add(m.group());
        }
        Map<String, Object> node = root;
        
        for (int i = 0; i < parts.size(); i++) {
            Object o = node.get(parts.get(i));
            
            // Found our target!
            if (i == parts.size() - 1) {
                node.put(parts.get(i), value);
                return;
            }
            
            if (o == null || !(o instanceof Map)) {
                // This will override existing configuration data!
                o = new HashMap<String, Object>();
                node.put(parts.get(i), o);
            }
            
            node = (Map<String, Object>)o;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void removeProperty(String path) {
        Matcher m = splitter.matcher(path);
        List<String> parts = new ArrayList<String>();
        while(m.find())
        {
            parts.add(m.group());
        }
        Map<String, Object> node = root;
        
        for (int i = 0; i < parts.size(); i++) {
            Object o = node.get(parts.get(i));
            
            // Found our target!
            if (i == parts.size() - 1) {
                node.remove(parts.get(i));
                return;
            }
            
            node = (Map<String, Object>)o;
        }
    }
}
