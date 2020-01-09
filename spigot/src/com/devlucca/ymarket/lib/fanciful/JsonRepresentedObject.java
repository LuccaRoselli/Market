package com.devlucca.ymarket.lib.fanciful;

import com.google.gson.stream.*;
import java.io.*;

interface JsonRepresentedObject
{
    void writeJson(final JsonWriter p0) throws IOException;
}
