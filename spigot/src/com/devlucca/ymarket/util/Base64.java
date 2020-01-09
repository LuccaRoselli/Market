package com.devlucca.ymarket.util;

import org.bukkit.inventory.*;
import org.yaml.snakeyaml.external.biz.base64Coder.*;
import java.util.*;
import org.bukkit.util.io.*;
import java.io.*;

public class Base64
{
    public static String toBase64(final List<ItemStack> itens) {
        String base64 = "";
        try {
            if (itens != null && !itens.isEmpty()) {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);
                for (final ItemStack item : itens) {
                    if (item != null) {
                        dataOutput.writeObject((Object)item);
                    }
                }
                dataOutput.close();
                base64 = Base64Coder.encodeLines(outputStream.toByteArray());
            }
        }
        catch (Exception erro) {
            erro.printStackTrace();
        }
        return base64;
    }
    
    public static List<ItemStack> fromBase64(final String base64) {
        final List<ItemStack> itens = new ArrayList<ItemStack>();
        try {
            if (base64 != null && !base64.isEmpty() && !base64.equalsIgnoreCase("null")) {
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
                final BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);
                ItemStack item = (ItemStack)dataInput.readObject();
                while (item != null) {
                    itens.add(item);
                    try {
                        item = (ItemStack)dataInput.readObject();
                    }
                    catch (Exception e) {
                        item = null;
                    }
                }
                dataInput.close();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return itens;
    }
}
