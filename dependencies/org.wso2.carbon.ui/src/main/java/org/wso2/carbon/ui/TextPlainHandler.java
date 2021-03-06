/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.net.URLConnection;

/**
 *
 */
public class TextPlainHandler extends ContentHandler {

    public Object getContent(URLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        InputStreamReader r = new InputStreamReader(in);
        StringBuffer sb = new StringBuffer();
        int c;
        while ((c = r.read()) >= 0) {
            sb.append((char) c);
        }
        r.close();
        in.close();
        return sb.toString();
    }
}
