/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;


public interface CompilerFacade extends Remote {

    enum OutputFormat implements java.io.Serializable {
        PLAIN,
        XML
    }

    interface RemoteIncrementalCache extends Remote {
        Collection<String> getObsoletePackageParts() throws RemoteException;;

        byte[] getPackageData(String fqName) throws RemoteException;;

        void close() throws RemoteException;
    }


    int remoteCompile(String[] args, RemoteOutputStream errStream, OutputFormat outputFormat) throws RemoteException;

    int remoteIncrementalCompile(
            String[] args,
            Map<String, RemoteIncrementalCache> caches,
            RemoteOutputStream outputStream,
            OutputFormat outputFormat
    ) throws RemoteException;
}
